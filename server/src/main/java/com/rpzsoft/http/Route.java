package com.rpzsoft.http;

import com.rpzsoft.handler.ReqHandler;

/**
 * Chainable builder returned by {@link Router#route(String)}, mirroring Express'
 * {@code app.route(path).get(...).post(...)}.
 */
public class Route {

    private final Router router;
    private final String path;

    Route(Router router, String path) {
        this.router = router;
        this.path = path;
    }

    public Route get(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.GET, handlers);
        return this;
    }

    public Route get(Middleware... handlers) {
        router.addRoute(path, HttpMethod.GET, handlers);
        return this;
    }

    public Route post(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.POST, handlers);
        return this;
    }

    public Route post(Middleware... handlers) {
        router.addRoute(path, HttpMethod.POST, handlers);
        return this;
    }

    public Route put(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.PUT, handlers);
        return this;
    }

    public Route put(Middleware... handlers) {
        router.addRoute(path, HttpMethod.PUT, handlers);
        return this;
    }

    public Route delete(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.DELETE, handlers);
        return this;
    }

    public Route delete(Middleware... handlers) {
        router.addRoute(path, HttpMethod.DELETE, handlers);
        return this;
    }

    public Route patch(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.PATCH, handlers);
        return this;
    }

    public Route patch(Middleware... handlers) {
        router.addRoute(path, HttpMethod.PATCH, handlers);
        return this;
    }

    public Route head(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.HEAD, handlers);
        return this;
    }

    public Route head(Middleware... handlers) {
        router.addRoute(path, HttpMethod.HEAD, handlers);
        return this;
    }

    public Route options(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.OPTIONS, handlers);
        return this;
    }

    public Route options(Middleware... handlers) {
        router.addRoute(path, HttpMethod.OPTIONS, handlers);
        return this;
    }

    public Route all(ReqHandler... handlers) {
        router.addRoute(path, HttpMethod.ANY, handlers);
        return this;
    }

    public Route all(Middleware... handlers) {
        router.addRoute(path, HttpMethod.ANY, handlers);
        return this;
    }
}
