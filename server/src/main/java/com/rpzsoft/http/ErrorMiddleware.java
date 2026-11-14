package com.rpzsoft.http;

import com.rpzsoft.common.Context;
import com.rpzsoft.common.Json;

/**
 * Express-style error-handling middleware (the 4-argument form). Invoked when a preceding handler
 * throws or calls {@link Next#next(Throwable)}.
 *
 * <p>Handling the error means producing a response and returning; call
 * {@link Next#next(Throwable)} to keep propagating, or {@link Next#next()} to resume the normal
 * chain.
 */
@FunctionalInterface
public interface ErrorMiddleware {

    void handle(Throwable err, Context ctx, Json req, Next next) throws Exception;
}
