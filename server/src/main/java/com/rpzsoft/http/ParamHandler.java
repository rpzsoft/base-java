package com.rpzsoft.http;

import com.rpzsoft.common.Context;

/**
 * Route parameter callback, mirroring Express' {@code router.param(name, fn)}. Runs once per
 * matching request before the route handlers, once for every registered callback on that parameter.
 */
@FunctionalInterface
public interface ParamHandler {

    void handle(Context ctx, String value, String name, Next next) throws Exception;
}
