package com.rpz.handler;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import com.rpz.common.OpsException;
import com.rpz.common.OpsStatus;
import com.rpz.common.Context;
import com.rpz.common.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 */
public class ReqHandlerBase implements ReqHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final Json exec(Context ctx, Json jreq) throws Exception{
        logger.debug("{} - {}", ctx, jreq);

        Json res = null;
        switch (ctx.method){
            case GET: res = read(ctx, null); break;
            case POST: res = create(ctx, jreq); break;
            case PUT: res = update(ctx, jreq); break;
            case DELETE: res = delete(ctx, jreq); break;
            case PATCH: res = patch(ctx, jreq); break;
            case OPTIONS: res = cors(ctx.req, ctx.res); break;
            default: throw new IllegalArgumentException("Invalid HTTP Method");
        }
        return res;
    }

    public Json create(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }

    public Json read(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }

    public Json find(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }

    public Json update(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }

    //partial updates
    public Json patch(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }

    public Json delete(Context ctx, Json req) throws Exception{
        throw new OpsException(OpsStatus.FORBIDDEN);
    }

    private Json cors(HttpRequest req, HttpResponse res) throws IOException {
        HttpHeaders headers = res.headers();
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type, Authorization");
        headers.set("Access-Control-Max-Age", "86400");
        return null;
    }
}
