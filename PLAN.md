# PLAN.md — tui harness MVP

Owner decisions: repo `tui` lowercase, public, MIT, Clerk default with Keycloak swap path, include Cloudflare edge.

## M0 Scaffold (this commit)
- deps.edn, src/tui/{core,auth,zen,tools,stream}, test/* + parallel runner, README, LICENSE, demo.tape, install.sh

## M1 OIDC auth
- Ring/Jetty loopback :8080, JWKS cache validate iss/aud/exp, issue 8h JWT. Protocol AuthProvider for :clerk/:auth0/:keycloak.

## M2 Zen proxy + streaming
- babashka.curl adapter, core.async streaming, SIGINT abort, prune to 32k tokens.

## M3 Edge telemetry (Cloudflare Worker)
- Worker = ingest + verify + rollback only. No agent loops (CPU caps). wrangler rollback, Vectorize fan-out.

## M4 Packaging
- tools.build uberjar + GraalVM native-image, install.sh, vhs demo.gif, CI license check.

## Attestation gate
`clj -M:test` must report TOTAL fail=0 error=0 else block push.
