package com.rpzsoft.http;

import com.rpzsoft.common.Context;
import com.rpzsoft.common.Json;
import com.rpzsoft.handler.ReqHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Express.js inspired HTTP router and middleware stack.
 *
 * <p>Handlers are evaluated in registration order. A request flows through every matching layer:
 * middleware (registered with {@link #use}) runs for any method on a path prefix, while route
 * handlers ({@link #get}, {@link #post}, ...) match the whole path and a specific method. Calling
 * {@link Next#next()} continues, {@link Next#next(Throwable)} jumps to error middleware, and
 * {@link Next#next(String)} (with {@link Next#ROUTE}) skips the rest of the current route.
 *
 * <p>A router may be mounted on another one with {@link #use(String, Router)}; the mount prefix is
 * stripped for the child and parameter values are merged.
 */
public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final List<Layer> layers = new CopyOnWriteArrayList<>();
    private final Map<String, List<ParamHandler>> paramHandlers = new ConcurrentHashMap<>();
    private final boolean caseSensitive;

    public Router() {
        this(false);
    }

    public Router(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /* ------------------------------------------------------------------ middleware */

    public Router use(Middleware... handlers) {
        return use("/", handlers);
    }

    public Router use(String path, Middleware... handlers) {
        add(path, null, true, handlers);
        return this;
    }

    /** Allow legacy {@link ReqHandler}s to be mounted as middleware. */
    public Router use(ReqHandler... handlers) {
        return use("/", handlers);
    }

    public Router use(String path, ReqHandler... handlers) {
        add(path, null, true, handlers);
        return this;
    }

    public Router use(ErrorMiddleware... handlers) {
        return use("/", handlers);
    }

    public Router use(String path, ErrorMiddleware... handlers) {
        add(path, null, true, handlers);
        return this;
    }

    /** Mount a sub-router at {@code path}. */
    public Router use(Router router) {
        return use("/", router);
    }

    public Router use(String path, Router router) {
        List<Object> hs = new ArrayList<>(1);
        hs.add(router);
        layers.add(new Layer(path, null, true, caseSensitive, hs));
        return this;
    }

    /* ------------------------------------------------------------------ routes */

    public Router get(String path, ReqHandler... handlers) {
        add(path, HttpMethod.GET, false, handlers);
        return this;
    }

    public Router get(String path, Middleware... handlers) {
        add(path, HttpMethod.GET, false, handlers);
        return this;
    }

    public Router post(String path, ReqHandler... handlers) {
        add(path, HttpMethod.POST, false, handlers);
        return this;
    }

    public Router post(String path, Middleware... handlers) {
        add(path, HttpMethod.POST, false, handlers);
        return this;
    }

    public Router put(String path, ReqHandler... handlers) {
        add(path, HttpMethod.PUT, false, handlers);
        return this;
    }

    public Router put(String path, Middleware... handlers) {
        add(path, HttpMethod.PUT, false, handlers);
        return this;
    }

    public Router delete(String path, ReqHandler... handlers) {
        add(path, HttpMethod.DELETE, false, handlers);
        return this;
    }

    public Router delete(String path, Middleware... handlers) {
        add(path, HttpMethod.DELETE, false, handlers);
        return this;
    }

    public Router patch(String path, ReqHandler... handlers) {
        add(path, HttpMethod.PATCH, false, handlers);
        return this;
    }

    public Router patch(String path, Middleware... handlers) {
        add(path, HttpMethod.PATCH, false, handlers);
        return this;
    }

    public Router options(String path, ReqHandler... handlers) {
        add(path, HttpMethod.OPTIONS, false, handlers);
        return this;
    }

    public Router options(String path, Middleware... handlers) {
        add(path, HttpMethod.OPTIONS, false, handlers);
        return this;
    }

    public Router head(String path, ReqHandler... handlers) {
        add(path, HttpMethod.HEAD, false, handlers);
        return this;
    }

    public Router head(String path, Middleware... handlers) {
        add(path, HttpMethod.HEAD, false, handlers);
        return this;
    }

    /** Register handlers for every HTTP method. */
    public Router all(String path, ReqHandler... handlers) {
        add(path, HttpMethod.ANY, false, handlers);
        return this;
    }

    public Router all(String path, Middleware... handlers) {
        add(path, HttpMethod.ANY, false, handlers);
        return this;
    }

    /**
     * Register a route parameter callback, run once per matching request before the route handlers
     * (Express' {@code router.param}).
     */
    public Router param(String name, ParamHandler handler) {
        paramHandlers.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(handler);
        return this;
    }

    /** Returns a builder for chaining several methods on the same path. */
    public Route route(String path) {
        return new Route(this, path);
    }

    void addRoute(String path, HttpMethod method, Object[] handlers) {
        add(path, method, false, handlers);
    }

    /* ------------------------------------------------------------------ dispatch */

    /** Dispatch a request through this router. {@code out} is invoked when nothing handled it. */
    public void handle(Context ctx, Json req, Next out) throws Exception {
        dispatchLayer(0, null, ctx, req, out);
    }

    public void handle(Context ctx, Next out) throws Exception {
        dispatchLayer(0, null, ctx, ctx == null ? null : ctx.in, out);
    }

    private void dispatchLayer(int index, Throwable err, Context ctx, Json req, Next out) throws Exception {
        if (index >= layers.size()) {
            if (err != null) out.next(err);
            else out.next();
            return;
        }

        Layer layer = layers.get(index);
        PathMatcher.Match m = layer.matcher.match(ctx.path);
        boolean methodOk = layer.method == null
                || layer.method == HttpMethod.ANY
                || layer.method == ctx.method
                || (ctx.method == HttpMethod.HEAD && layer.method == HttpMethod.GET);

        if (m == null || !methodOk) {
            dispatchLayer(index + 1, err, ctx, req, out);
            return;
        }

        Snapshot snap = Snapshot.of(ctx);
        ctx.params = mergeParams(ctx.params, m);
        ctx.pnames = mergeNames(ctx.pnames, m.names);

        if (layer.prefix && m.consumed > 0) {
            mount(ctx, m.consumed);
        }

        List<Object> effective = withParamCallbacks(layer, m, err);
        dispatchHandler(layer, index, effective, 0, err, ctx, req, snap, out);
    }

    private void dispatchHandler(Layer layer, int layerIndex, List<Object> handlers, int hi, Throwable err,
                                 Context ctx, Json req, Snapshot snap, Next out) throws Exception {
        if (hi >= handlers.size()) {
            snap.restore(ctx);
            dispatchLayer(layerIndex + 1, err, ctx, req, out);
            return;
        }

        Object handler = handlers.get(hi);
        Next next = new Next() {
            @Override
            public void next() throws Exception {
                dispatchHandler(layer, layerIndex, handlers, hi + 1, null, ctx, req, snap, out);
            }

            @Override
            public void next(Throwable e) throws Exception {
                dispatchHandler(layer, layerIndex, handlers, hi + 1, e, ctx, req, snap, out);
            }

            @Override
            public void next(String route) throws Exception {
                snap.restore(ctx);
                dispatchLayer(layerIndex + 1, null, ctx, req, out);
            }
        };

        try {
            if (err != null) {
                if (handler instanceof ErrorMiddleware) {
                    ((ErrorMiddleware) handler).handle(err, ctx, req, next);
                    return;
                }
                next.next(err);
                return;
            }

            if (handler instanceof ErrorMiddleware) {
                // error handlers are skipped during normal flow
                next.next();
            } else if (handler instanceof Middleware) {
                ((Middleware) handler).handle(ctx, req, next);
            } else if (handler instanceof Router) {
                Router sub = (Router) handler;
                Next subOut = new Next() {
                    @Override
                    public void next() throws Exception {
                        snap.restore(ctx);
                        dispatchHandler(layer, layerIndex, handlers, hi + 1, null, ctx, req, snap, out);
                    }

                    @Override
                    public void next(Throwable e) throws Exception {
                        snap.restore(ctx);
                        dispatchHandler(layer, layerIndex, handlers, hi + 1, e, ctx, req, snap, out);
                    }

                    @Override
                    public void next(String route) throws Exception {
                        snap.restore(ctx);
                        dispatchLayer(layerIndex + 1, null, ctx, req, out);
                    }
                };
                sub.handle(ctx, req, subOut);
            } else if (handler instanceof ReqHandler) {
                Json ret = ((ReqHandler) handler).exec(ctx, req);
                if (ret != null) {
                    ctx.out = ret;
                } else if (ctx.out == null && ctx.status == HttpStatus.OK) {
                    ctx.status = HttpStatus.NO_CONTENT;
                }
            } else {
                logger.warn("Unknown handler type: {}", handler.getClass());
                next.next();
            }
        } catch (Throwable t) {
            next.next(t);
        }
    }

    private List<Object> withParamCallbacks(Layer layer, PathMatcher.Match m, Throwable err) {
        if (err != null || paramHandlers.isEmpty() || m.names.isEmpty()) return layer.handlers;

        List<Object> effective = new ArrayList<>(layer.handlers.size() + m.names.size());
        for (int i = 0; i < m.names.size(); i++) {
            String name = m.names.get(i);
            String value = m.values.get(i);
            List<ParamHandler> callbacks = paramHandlers.get(name);
            if (callbacks == null) continue;
            for (ParamHandler callback : callbacks) {
                effective.add((Middleware) (c, r, n) -> callback.handle(c, value, name, n));
            }
        }
        if (effective.isEmpty()) return layer.handlers;
        effective.addAll(layer.handlers);
        return effective;
    }

    private void mount(Context ctx, int consumed) {
        String prefix = ctx.path.substring(0, consumed);
        String rest = ctx.path.substring(consumed);
        ctx.baseUrl = ctx.baseUrl + (prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix);
        ctx.path = rest.isEmpty() ? "/" : rest;
    }

    private static Json mergeParams(Json base, PathMatcher.Match m) {
        Json merged = new Json();
        if (base != null) merged.putAll(base);
        for (int i = 0; i < m.names.size(); i++) {
            merged.put(m.names.get(i), m.values.get(i));
        }
        return merged;
    }

    private static List<String> mergeNames(List<String> base, List<String> names) {
        List<String> merged = new ArrayList<>(names.size() + (base == null ? 0 : base.size()));
        if (base != null) merged.addAll(base);
        merged.addAll(names);
        return merged;
    }

    private void add(String path, HttpMethod method, boolean prefix, Object... handlers) {
        List<Object> flat = new ArrayList<>();
        for (Object h : handlers) {
            if (h instanceof Object[]) {
                for (Object o : (Object[]) h) {
                    if (o != null) flat.add(o);
                }
            } else if (h != null) {
                flat.add(h);
            }
        }
        if (flat.isEmpty()) throw new IllegalArgumentException("At least one handler is required for " + path);
        layers.add(new Layer(path, method, prefix, caseSensitive, flat));
    }

    /** Adapt a terminal {@link ReqHandler} so it can be mixed into a middleware chain. */
    public static Middleware of(ReqHandler handler) {
        return (ctx, req, next) -> {
            Json ret = handler.exec(ctx, req);
            if (ret != null) ctx.out = ret;
        };
    }

    /** Saved request routing state, restored when leaving a layer (Express restores {@code req.url}). */
    private static final class Snapshot {
        final String path;
        final String baseUrl;
        final Json params;
        final List<String> pnames;

        private Snapshot(Context ctx) {
            this.path = ctx.path;
            this.baseUrl = ctx.baseUrl;
            this.params = ctx.params;
            this.pnames = ctx.pnames;
        }

        static Snapshot of(Context ctx) {
            return new Snapshot(ctx);
        }

        void restore(Context ctx) {
            ctx.path = path;
            ctx.baseUrl = baseUrl;
            ctx.params = params;
            ctx.pnames = pnames;
        }
    }
}
