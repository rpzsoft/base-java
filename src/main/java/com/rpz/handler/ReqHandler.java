package com.rpz.handler;

import com.rpz.common.Context;
import com.rpz.common.Json;


public interface ReqHandler {
   Json exec(Context ctx, Json jreq) throws Exception;

   /*Json create(Context ctx, Json req) throws Exception;

   Json read(Context ctx, Json req) throws Exception;

   Json update(Context ctx, Json req) throws Exception;

    //partial updates
   Json patch(Context ctx, Json req) throws Exception;

   Json delete(Context ctx, Json req) throws Exception;*/
}
