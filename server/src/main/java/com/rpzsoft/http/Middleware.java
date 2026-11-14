package com.rpzsoft.http;

import com.rpzsoft.common.Context;
import com.rpzsoft.common.Json;

/**
 * Express-style middleware. Call {@link Next#next()} to continue the chain, set
 * {@link Context#out}/{@link Context#status} (or write to {@link Context#res}) and simply return to
 * end the request, or call {@link Next#next(Throwable)} to hand control to error middleware.
 *
 * <p>Register via {@link Router#use(Object...)} or as part of a route
 * ({@link Router#get(String, Middleware...)}).
 */
@FunctionalInterface
public interface Middleware {

    void handle(Context ctx, Json req, Next next) throws Exception;
}
