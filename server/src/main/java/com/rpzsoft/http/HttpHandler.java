package com.rpzsoft.http;


import com.rpzsoft.service.SampleService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import com.rpzsoft.common.OpsException;
import com.rpzsoft.common.OpsRuntimeException;
import com.rpzsoft.common.OpsStatus;
import com.rpzsoft.common.Conf;
import com.rpzsoft.common.Context;
import com.rpzsoft.common.Json;
import com.rpzsoft.common.Vld;
import com.rpzsoft.handler.*;
import com.rpzsoft.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static com.rpzsoft.common.OpsStatus.NOT_ACCEPTABLE;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    static final Conf CONF = Conf.getInstance();
    static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final AuthService authService = new AuthService();

    /** Application router. Middleware and routes are declared in the static initializer below. */
    public static final Router app = new Router();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext cctx, FullHttpRequest req) throws Exception {


        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
        HttpHeaders headers = res.headers();
        headers.set(DATE, df.format(OffsetDateTime.now()));
        headers.set(SERVER, CONF.getProperty("app.name"));
        boolean keepAlive = HttpUtil.isKeepAlive(req);

        try {

            if (!req.decoderResult().isSuccess()) {
                throw new OpsException(OpsStatus.BAD_REQUEST, "Invalid Request");
            }

            headers.set(CONTENT_TYPE, APPLICATION_JSON);
            headers.set("Access-Control-Allow-Origin", "*");

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    headers.set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }
            } else {
                headers.set(CONNECTION, CLOSE);
            }

            final String method = req.method().name();
            URI uri = URI.create(req.uri());
            String path = uri.getPath();

            req.content().resetReaderIndex();
            final int size = req.content().readableBytes();
            logger.debug("{} {} ", method, req.uri());


            if(path.endsWith("/favicon.ico"))
                throw new OpsException(OpsStatus.NOT_FOUND);

            authService.auth(req);

            Context ctx = new Context(method, path);
            Json jreq = null;

            if(size > 0) {
                String ctype = req.headers().get(CONTENT_TYPE);
                if(ctype == null || !ctype.startsWith("application/json"))
                    throw new OpsRuntimeException(NOT_ACCEPTABLE);
                byte [] buf = new byte[size];
                req.content().readBytes(buf);
                jreq = Json.fromJson(buf);
            }

            ctx.query = uri.getQuery();
            ctx.headers = req.headers();
            ctx.req = req;
            ctx.res = res;
            ctx.in = jreq;

            logger.debug("{} - {}", ctx, jreq);

            // Running off the end of the chain without a response means "no route matched".
            final Next done = new Next() {
                @Override
                public void next() throws Exception {
                    throw new OpsException(OpsStatus.NOT_FOUND);
                }

                @Override
                public void next(String route) throws Exception {
                    next();
                }

                @Override
                public void next(Throwable e) throws Exception {
                    if (e instanceof OpsException) throw (OpsException) e;
                    if (e instanceof OpsRuntimeException) {
                        OpsRuntimeException re = (OpsRuntimeException) e;
                        throw new OpsException(re.getOpsStatus(), re.getMessage(), e);
                    }
                    if (e instanceof Exception) throw (Exception) e;
                    throw new OpsRuntimeException(OpsStatus.INTERNAL_SERVER_ERROR, String.valueOf(e.getMessage()), e);
                }
            };

            app.handle(ctx, jreq, done);

            if(ctx.response != null) {
                Response out = ctx.response;
                res.setStatus(HttpResponseStatus.valueOf(out.status));
                headers.set(CONTENT_TYPE, out.contentType);
                for (java.util.Map.Entry<String, String> e : out.headers.entrySet()) {
                    headers.set(e.getKey(), e.getValue());
                }
                headers.setInt(CONTENT_LENGTH, out.body.length);
                res.content().writeBytes(out.body);
            } else {
                res.setStatus(HttpResponseStatus.valueOf(ctx.status.code()));
                if(ctx.out != null) {
                    final byte [] buf = ctx.out.toString().getBytes();
                    headers.setInt(CONTENT_LENGTH, buf.length);
                    res.content().writeBytes(buf);
                } else {
                    headers.setInt(CONTENT_LENGTH, 0);
                }
            }

        } catch (Exception e) {
            HttpStatus status = ErrorMapper.status(e);
            String err = ErrorMapper.message(e, status);
            if (status.isServerError()) {
                logger.error("{} {} -> {}", req.method(), req.uri(), err, e);
            } else {
                logger.info("{} {} -> {}", req.method(), req.uri(), err);
            }
            res.setStatus(HttpResponseStatus.valueOf(status.code()));
            writeError(headers, res, err);
        }

        ChannelFuture f = cctx.write(res);

        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void writeError(HttpHeaders headers, FullHttpResponse res, String err) {
        final byte [] buf = new Json("err", err).toString().getBytes();
        headers.setInt(CONTENT_LENGTH, buf.length);
        res.content().writeBytes(buf);
    }

    public static final String BASE_EP="/api/v1";

    static {
        // --- global middleware (Express app.use) ---
        app.use((ctx, req, next) -> {
            logger.debug("--> {} {}{}", ctx.method, ctx.baseUrl, ctx.path);
            next.next();
        });

        // run before any route that declares an :id parameter
        app.param("id", (ctx, value, name, next) -> {
            ctx.put("id", value);
            next.next();
        });

        // --- routes ---
        final SampleService ssvc = new SampleService();
        SampleReqHandler srh = new SampleReqHandler(ssvc);

        app.get(BASE_EP+"/test", srh::find);
        app.get(BASE_EP+"/test/:id", srh::read);
        app.post(BASE_EP+"/test", srh::create);
        app.put(BASE_EP+"/test/:id", srh::update);
        app.delete(BASE_EP+"/test/:id", srh::delete);
        app.post(BASE_EP+"/test/auth/:svc", srh::auth);
        app.post(BASE_EP+"/test/token/:svc", srh::token);
        app.post(BASE_EP+"/test/conf/:svc", srh::conf);
        app.post(BASE_EP+"/test/:id", srh::invoke);

        // inline middleware routes showing path params and a full Response
        app.get(BASE_EP+"/hello/:name", (Middleware) (ctx, req, next) ->
                ctx.send(new Json("hello", ctx.param("name"))));
        app.get(BASE_EP+"/ping", (Middleware) (ctx, req, next) ->
                ctx.send(Response.text("pong")));

        ReqHandler handler = new DefaultReqHandler(new AuthService());
        app.get(BASE_EP+"/auth", handler);
        app.get(BASE_EP+"/auth/:svc", handler);
        app.delete(BASE_EP+"/auth/:svc", handler);

        // CORS preflight
        app.options("*", new ReqHandlerBase());

        // --- error middleware (Express 4-arg form); must be last ---
        app.use((ErrorMiddleware) (err, ctx, req, next) -> {
            HttpStatus status = ErrorMapper.status(err);
            String msg = ErrorMapper.message(err, status);
            if (status.isServerError()) {
                logger.error("{} {} -> {}", ctx.method, ctx.path, msg, err);
            } else {
                logger.info("{} {} -> {}", ctx.method, ctx.path, msg);
            }
            ctx.status(status).send(new Json("err", msg));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String msg = cause.getMessage();
        if(Vld.isEmpty(msg))
            logger.warn("{}", cause);
        else logger.warn("{}", msg);
        ctx.close();
    }
}
