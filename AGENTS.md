# AGENTS.md

Two independent projects in one repo. There is no root build/workspace, no CI, no linter, and no formatter.

## Layout
- `server/` — Java 17 + Netty HTTP/WebSocket server, Maven (no wrapper). Entrypoint `com.rpz.Server`.
- `client/` — Vite + vanilla TypeScript frontend.
- `docker-compose.yml` — local Postgres 17 (host port **5657**, db/user/pass `rpz`/`postgres`/`password`) and Valkey (host **6357** → 6379). Volumes `./db`, `./redis`.

## Server (`server/`)
- **Java 17** per the pom (`maven.compiler.source/target=17`); the README's "Java 21" is stale. Verified with the default JDK.
- Commands (run inside `server/`): `mvn clean test`, `mvn -DskipTests package`, single test `mvn -Dtest=RouterTest test`. Use `clean` after switching JDKs: stale classes from another JDK make surefire fail with a cryptic "forked VM terminated" error.
- Run from the `server/` directory because `app.home=.` makes the CWD the home dir, so TLS certs resolve to `server/etc/{cert,key}.pem` and logs land under `server/`. Running elsewhere changes those paths.
- Set `-DLOG_FILES_DIR=logs` when running. `logback.xml` has no default for `LOG_FILES_DIR`; without it logback creates a literal `${LOG_FILES_DIR}` directory.
- Defaults: `app.ssl=true`, binds `app.host:app.port` = `0.0.0.0:9957`, uses `server/etc/cert.pem`+`key.pem` (gitignored) or a self-signed cert if the paths are empty. WebSocket endpoint `/ws`.
- Express-style routing/middleware lives in `com.rpz.http`. `HttpHandler.app` (a `Router` instance) is configured in the `HttpHandler` **static initializer**, not a separate config, under `BASE_EP="/api/v1"`. Chain: `HttpHandler` → `Router` layers → `ReqHandler`/`Middleware` → `BaseService` → `RepoService`.
- Router API: `use([path], ...)` middleware (path prefix, any method), `get/post/put/delete/patch/options/head/all(path, ...)` routes, `route(path).get(...).post(...)`, `param(name, fn)` callbacks, and `use(path, childRouter)` sub-router mounting (strips the prefix, merges params). Paths support `:name`, `:name?`, `:name*`, `:name+`, `:name(regex)`, and `*`/`*name` splats; matching is case-insensitive unless `new Router(true)`.
- Middleware signature is `(Context, Json req, Next)`; call `next.next()`, `next.next(err)`, or `next.next(Next.ROUTE)`. Error middleware (`ErrorMiddleware`) is `(Throwable, Context, Json, Next)`, registered with `use(...)` and must be last. Respond via `ctx.out`/`ctx.status`/`ctx.send(Json)`, or `ctx.send(Response)`. Falling off the chain with no response yields 404.
- All thrown errors go through `ErrorMapper`: `OpsException`/`OpsRuntimeException` keep their status/message, `IllegalArgumentException` becomes 400, and **any other throwable (e.g. the `repo` NPE) is logged with a stack trace at ERROR and returned as `500 {"err":"Internal Server Error"}`** so internals never leak to clients.
- Stubs to know: `RepoService` has **no implementation** and `BaseService.repo` is never assigned, so any DB-backed call NPEs. `SampleService`/`AuthService` methods are placeholders. `RouterTest` in `com.rpz.http` is the meaningful suite; `ServerTest` is the default trivial JUnit4 test. `Response` is wired via `ctx.send(Response)`; `Request` is currently unused.
- Config: `src/main/resources/app.properties` is loaded by `Conf`. `app.conf` lists extra property files (`db.properties`, `auth.properties`) loaded only by `ConfigRegistry`, not by `Conf`. The `cors.*` properties and `web.host`/`web.port`/`app.oauth.redirect` are referenced nowhere effective / undefined; CORS is hardcoded in `HttpHandler`.

## Client (`client/`)
- `npm install`, then `npm run dev` (port 5173) or `npm run build` (`tsc && vite build`). No test or lint script.
- `vite.config.ts` proxies `/api` to hardcoded `http://10.1.1.10:9957` — change it to reach a local server, and note the server defaults to HTTPS.
- `tsconfig.json` sets `noUnusedLocals`/`noUnusedParameters`, so `npm run build` fails on unused code.
