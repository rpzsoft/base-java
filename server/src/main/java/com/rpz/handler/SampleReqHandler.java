package com.rpz.handler;

import com.rpz.common.Context;
import com.rpz.common.Json;
import com.rpz.service.BaseService;
import com.rpz.service.SampleService;


public class SampleReqHandler extends DefaultReqHandler {

    public SampleReqHandler(BaseService service) {
        super(service);
    }

    public Json auth(Context ctx, Json req) throws Exception {
        logger.debug("auth");
        return ((SampleService) service).auth(ctx, req);
    }

    public Json token(Context ctx, Json req) throws Exception {
        logger.debug("token");
        return ((SampleService) service).token(ctx, req);
    }

    public Json conf(Context ctx, Json req) throws Exception {
        logger.debug("conf");
        return ((SampleService) service).conf(ctx, req);
    }

    public Json invoke(Context ctx, Json req) throws Exception {
        logger.debug("invoke");

        Json res = new Json();
        ((SampleService) service).invoke(ctx, req);
        return res;
    }
}
