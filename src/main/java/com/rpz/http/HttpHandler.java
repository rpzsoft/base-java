package com.rpz.http;


import com.rpz.service.SampleService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import com.rpz.common.OpsException;
import com.rpz.common.OpsRuntimeException;
import com.rpz.common.OpsStatus;
import com.rpz.common.Conf;
import com.rpz.common.Context;
import com.rpz.common.Json;
import com.rpz.common.Vld;
import com.rpz.handler.*;
import com.rpz.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static com.rpz.common.OpsStatus.NOT_ACCEPTABLE;
import static com.rpz.http.Router.*;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    static final Conf CONF = Conf.getInstance();
    static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final AuthService authService = new AuthService();
    static final Set<ReqHandler> handlers = new HashSet<>();

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
            headers.set("Access-Control-Allow-Origin", "*"); //since local server

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

            Context ctx = Router.match(method, path);

            if(ctx == null || ctx.handler == null) {
                logger.info("FORBIDDEN: {} {}", method,  path);
                throw new OpsException(OpsStatus.FORBIDDEN);
            }

            ReqHandler handler = ctx.handler;
            Json jreq = null;

            if(size > 0) {
                if(!req.headers().get("Content-Type").startsWith("application/json"))
                    throw new OpsRuntimeException(NOT_ACCEPTABLE);
                byte [] buf = new byte[size];
                req.content().readBytes(buf);
                jreq = Json.fromJson(buf);
            }

            ctx.query = uri.getQuery();
            ctx.headers = req.headers();
            ctx.req = req;
            ctx.res = res;

            logger.debug("{} - {}", ctx, jreq);

            Json ret = handler.exec(ctx, jreq);

            res.setStatus(ret == null?HttpResponseStatus.NO_CONTENT: OK);
            if(ret != null) {
                final byte [] buf = ret.toString().getBytes();
                headers.setInt(CONTENT_LENGTH, buf.length);
                res.content().writeBytes(buf);
            }

        } catch (OpsException e) {
            String err = e.getMessage();
            OpsStatus st = e.getStatus();
            if(err == null) err = st.toString();
            logger.info("{}", err, e);
            res.setStatus(HttpResponseStatus.valueOf(st.status()));
            String ret = Json.jsonStr("err", err);
            final byte [] buf = ret.getBytes();
            headers.setInt(CONTENT_LENGTH, buf.length);
            res.content().writeBytes(buf);
        } catch (Exception e) {
            String err = e.getMessage();
            logger.info("{}", err, e);
            res.setStatus(HttpResponseStatus.BAD_REQUEST);
            if(err == null) err = "Unexpected";
            String ret = Json.jsonStr("err", err);
            final byte[] buf = ret.getBytes();
            headers.setInt(CONTENT_LENGTH, buf.length);
            res.content().writeBytes(buf);
        }

        ChannelFuture f = cctx.write(res);

        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static final String BASE_EP="/api/v1";

    static {
        final SampleService ssvc = new SampleService();
        SampleReqHandler srh = new SampleReqHandler(ssvc);
        handlers.add(srh);

        get(BASE_EP+"/test", srh);
        get(BASE_EP+"/test/:id", srh::read);
        post(BASE_EP+"/test", srh);
        put(BASE_EP+"/test/:id", srh);
        delete(BASE_EP+"/test/:id", srh);
        post(BASE_EP+"/test/auth/:svc", srh::auth);
        post(BASE_EP+"/test/token/:svc", srh::token);
        post(BASE_EP+"/test/conf/:svc", srh::conf);
        post(BASE_EP+"/test/:id", srh::invoke);
        
        ReqHandler handler = new DefaultReqHandler(new AuthService());
        handlers.add(handler);
        get(BASE_EP+"/auth", handler);
        get(BASE_EP+"/auth/:svc", handler);
        delete(BASE_EP+"/auth/:svc", handler);

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