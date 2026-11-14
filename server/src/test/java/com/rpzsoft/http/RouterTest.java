package com.rpzsoft.http;

import com.rpzsoft.common.Context;
import com.rpzsoft.common.Json;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RouterTest {

    private static final Middleware SEND_EMPTY = (ctx, req, next) -> ctx.send(new Json("ok", true));

    private static final class Result {
        Context ctx;
        Json body;
        HttpStatus status;
        Throwable error;
        boolean notFound;
    }

    private Result run(Router router, String method, String path) throws Exception {
        Context ctx = new Context(method, path);
        Result res = new Result();
        Next done = new Next() {
            @Override
            public void next() {
                res.notFound = true;
            }

            @Override
            public void next(String route) {
                res.notFound = true;
            }

            @Override
            public void next(Throwable e) {
                res.error = e;
            }
        };
        router.handle(ctx, null, done);
        res.ctx = ctx;
        res.body = ctx.out;
        res.status = ctx.status;
        return res;
    }

    @Test
    public void extractsPathParams() throws Exception {
        Router r = new Router();
        r.get("/users/:id", (Middleware) (ctx, req, next) -> ctx.send(new Json("id", ctx.param("id"))));

        Result out = run(r, "GET", "/users/42");
        assertFalse(out.notFound);
        assertEquals("42", out.body.getString("id"));
    }

    @Test
    public void methodMismatchFallsThrough() throws Exception {
        Router r = new Router();
        r.get("/users", SEND_EMPTY);

        assertTrue(run(r, "POST", "/users").notFound);
    }

    @Test
    public void middlewareRunsInOrderAndWrapsNext() throws Exception {
        List<String> order = new ArrayList<>();
        Router r = new Router();
        r.use((ctx, req, next) -> {
            order.add("before");
            next.next();
            order.add("after");
        });
        r.get("/x", (Middleware) (ctx, req, next) -> order.add("handler"));

        run(r, "GET", "/x");
        assertEquals(Arrays.asList("before", "handler", "after"), order);
    }

    @Test
    public void nextRouteSkipsRemainingHandlers() throws Exception {
        Router r = new Router();
        r.get("/x", (Middleware) (ctx, req, next) -> next.next(Next.ROUTE));
        r.get("/x", (Middleware) (ctx, req, next) -> ctx.send(new Json("second", true)));

        assertEquals(Boolean.TRUE, run(r, "GET", "/x").body.getBoolean("second"));
    }

    @Test
    public void errorMiddlewareHandlesThrownErrors() throws Exception {
        Router r = new Router();
        r.get("/boom", (Middleware) (ctx, req, next) -> {
            throw new IllegalStateException("boom");
        });
        r.use((ErrorMiddleware) (err, ctx, req, next) ->
                ctx.status(HttpStatus.BAD_REQUEST).send(new Json("err", err.getMessage())));

        Result out = run(r, "GET", "/boom");
        assertEquals(HttpStatus.BAD_REQUEST, out.status);
        assertEquals("boom", out.body.getString("err"));
    }

    @Test
    public void nextErrorGoesToErrorMiddleware() throws Exception {
        Router r = new Router();
        r.get("/e", (Middleware) (ctx, req, next) -> next.next(new IllegalStateException("x")));
        r.use((ErrorMiddleware) (err, ctx, req, next) -> ctx.send(new Json("caught", err.getMessage())));

        assertEquals("x", run(r, "GET", "/e").body.getString("caught"));
    }

    @Test
    public void unhandledErrorPropagatesToOut() throws Exception {
        Router r = new Router();
        r.get("/e", (Middleware) (ctx, req, next) -> next.next(new IllegalStateException("x")));

        assertNotNull(run(r, "GET", "/e").error);
    }

    @Test
    public void mountsSubRouterAndStripsPrefix() throws Exception {
        Router api = new Router();
        api.get("/users/:id", (Middleware) (ctx, req, next) ->
                ctx.send(new Json("id", ctx.param("id"), "base", ctx.baseUrl, "path", ctx.path)));

        Router app = new Router();
        app.use("/api", api);

        Result out = run(app, "GET", "/api/users/7");
        assertEquals("7", out.body.getString("id"));
        assertEquals("/api", out.body.getString("base"));
        assertEquals("/users/7", out.body.getString("path"));
    }

    @Test
    public void mountedRouterInheritsParentParams() throws Exception {
        Router api = new Router();
        api.get("/u/:id", (Middleware) (ctx, req, next) ->
                ctx.send(new Json("tenant", ctx.param("tenant"), "id", ctx.param("id"))));

        Router app = new Router();
        app.use("/:tenant", api);

        Result out = run(app, "GET", "/acme/u/9");
        assertEquals("acme", out.body.getString("tenant"));
        assertEquals("9", out.body.getString("id"));
    }

    @Test
    public void splatWildcardCapturesRest() throws Exception {
        Router r = new Router();
        r.get("/static/*", (Middleware) (ctx, req, next) -> ctx.send(new Json("rest", ctx.param("0"))));

        assertEquals("css/app.css", run(r, "GET", "/static/css/app.css").body.getString("rest"));
    }

    @Test
    public void namedWildcardCapturesRest() throws Exception {
        Router r = new Router();
        r.get("/files/*rest", (Middleware) (ctx, req, next) -> ctx.send(new Json("rest", ctx.param("rest"))));

        assertEquals("a/b.txt", run(r, "GET", "/files/a/b.txt").body.getString("rest"));
    }

    @Test
    public void optionalParamMatchesWithAndWithoutValue() throws Exception {
        Router r = new Router();
        r.get("/opt/:x?", (Middleware) (ctx, req, next) -> ctx.send(new Json("x", ctx.param("x"))));

        assertNull(run(r, "GET", "/opt").body.getString("x"));
        assertEquals("5", run(r, "GET", "/opt/5").body.getString("x"));
    }

    @Test
    public void plusParamRequiresAtLeastOneSegment() throws Exception {
        Router r = new Router();
        r.get("/files/:path+", (Middleware) (ctx, req, next) -> ctx.send(new Json("p", ctx.param("path"))));

        assertTrue(run(r, "GET", "/files").notFound);
        assertEquals("a/b", run(r, "GET", "/files/a/b").body.getString("p"));
    }

    @Test
    public void customRegexParamConstrainsMatch() throws Exception {
        Router r = new Router();
        r.get("/n/:id(\\d+)", (Middleware) (ctx, req, next) -> ctx.send(new Json("id", ctx.param("id"))));

        assertEquals("123", run(r, "GET", "/n/123").body.getString("id"));
        assertTrue(run(r, "GET", "/n/abc").notFound);
    }

    @Test
    public void paramCallbacksRunBeforeHandler() throws Exception {
        List<String> seen = new ArrayList<>();
        Router r = new Router();
        r.param("id", (ctx, value, name, next) -> {
            seen.add(name + "=" + value);
            next.next();
        });
        r.get("/p/:id", SEND_EMPTY);

        run(r, "GET", "/p/9");
        assertEquals(Arrays.asList("id=9"), seen);
    }

    @Test
    public void routeBuilderChainsMethods() throws Exception {
        Router r = new Router();
        r.route("/r")
                .get((Middleware) (ctx, req, next) -> ctx.send(new Json("m", "get")))
                .post((Middleware) (ctx, req, next) -> ctx.send(new Json("m", "post")));

        assertEquals("get", run(r, "GET", "/r").body.getString("m"));
        assertEquals("post", run(r, "POST", "/r").body.getString("m"));
    }

    @Test
    public void allMatchesAnyMethod() throws Exception {
        Router r = new Router();
        r.all("/a", SEND_EMPTY);

        assertFalse(run(r, "PUT", "/a").notFound);
        assertFalse(run(r, "DELETE", "/a").notFound);
    }

    @Test
    public void headFallsBackToGet() throws Exception {
        Router r = new Router();
        r.get("/h", SEND_EMPTY);

        assertFalse(run(r, "HEAD", "/h").notFound);
    }

    @Test
    public void trailingSlashIsOptional() throws Exception {
        Router r = new Router();
        r.get("/t", SEND_EMPTY);

        assertFalse(run(r, "GET", "/t/").notFound);
    }

    @Test
    public void middlewarePrefixMatchesSegmentBoundaryOnly() throws Exception {
        Router r = new Router();
        r.use("/api", (ctx, req, next) -> {
            ctx.put("hit", true);
            next.next();
        });
        r.get("/api/x", SEND_EMPTY);

        assertTrue(run(r, "GET", "/api/x").ctx.containsKey("hit"));
        assertTrue(run(r, "GET", "/apix").notFound);
    }

    @Test
    public void caseInsensitiveByDefaultAndSensitiveWhenRequested() throws Exception {
        Router insensitive = new Router();
        insensitive.get("/Users", SEND_EMPTY);
        assertFalse(run(insensitive, "GET", "/users").notFound);

        Router sensitive = new Router(true);
        sensitive.get("/Users", SEND_EMPTY);
        assertTrue(run(sensitive, "GET", "/users").notFound);
    }

    @Test
    public void legacyReqHandlerCanBeMountedAsMiddleware() throws Exception {
        Router r = new Router();
        r.get("/legacy", Router.of((ctx, req) -> new Json("legacy", true)));

        assertEquals(Boolean.TRUE, run(r, "GET", "/legacy").body.getBoolean("legacy"));
    }

    @Test
    public void middlewareCanSendFullResponse() throws Exception {
        Router r = new Router();
        r.get("/text", (Middleware) (ctx, req, next) -> ctx.send(Response.text("hi")));

        Result out = run(r, "GET", "/text");
        assertNotNull(out.ctx.response);
        assertEquals("text/plain; charset=utf-8", out.ctx.response.contentType);
        assertEquals("hi", new String(out.ctx.response.body, java.nio.charset.StandardCharsets.UTF_8));
    }
}
