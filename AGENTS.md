# AGENTS.md

Two independent projects in one repo. There is no root build/workspace, no CI, no linter, and no formatter.

## Layout
- `server/` — Java 17 + Netty HTTP/WebSocket server, Maven (no wrapper). Entrypoint `com.rpz.Server`.
- `client/` — Vite + vanilla TypeScript frontend.
- `docker-compose.yml` — local Postgres 17 (host port **5657**, db/user/pass `rpz`/`postgres`/`password`) and Valkey (host **6357** → 6379). Volumes `./db`, `./redis`.

## Server (`server/`)
- **Java 17 required** (`maven.compiler.source/target=17`). 
- Commands (run inside `server/`): `mvn -DskipTests package`, `mvn compile`, `mvn test`, single test `mvn -Dtest=ServerTest test`.
- Run from the `server/` directory because `app.home=.` makes the CWD the home dir, so TLS certs resolve to `server/etc/{cert,key}.pem` and logs land under `server/`. Running elsewhere changes those paths.
- Set `-DLOG_FILES_DIR=logs` when running. `logback.xml` has no default for `LOG_FILES_DIR`; without it logback creates a literal `${LOG_FILES_DIR}` directory.
- Defaults: `app.ssl=true`, binds `app.host:app.port` = `0.0.0.0:9957`, uses `server/etc/cert.pem`+`key.pem` (gitignored) or a self-signed cert if the paths are empty. WebSocket endpoint `/ws`.
- Routing lives in the `HttpHandler` **static initializer**, not a separate config: routes are registered through `Router` under `BASE_EP="/api/v1"` (`get/post/put/delete`, `:name` path params). Chain: `HttpHandler` → `Router.match` → `ReqHandler`/`ReqHandlerBase` → `BaseService` → `RepoService`.
- Stubs to know: `RepoService` has **no implementation** and `BaseService.repo` is never assigned, so any DB-backed call NPEs. `SampleService`/`AuthService` methods are placeholders; `ServerTest` is the default trivial JUnit4 test.
- Config: `src/main/resources/app.properties` is loaded by `Conf`. `app.conf` lists extra property files (`db.properties`, `auth.properties`) loaded only by `ConfigRegistry`, not by `Conf`. The `cors.*` properties and `web.host`/`web.port`/`app.oauth.redirect` are referenced nowhere effective / undefined; CORS is hardcoded in `HttpHandler`.

## Client (`client/`)
- `npm install`, then `npm run dev` (port 5173) or `npm run build` (`tsc && vite build`). No test or lint script.
- `vite.config.ts` proxies `/api` to hardcoded `http://10.1.1.10:9957` — change it to reach a local server, and note the server defaults to HTTPS.
- `tsconfig.json` sets `noUnusedLocals`/`noUnusedParameters`, so `npm run build` fails on unused code.
