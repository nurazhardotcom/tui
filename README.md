# tui — TUI `tui.nurazhar.com`

> **Status:** Active — personal prod gateway, dogfooded daily. See [AI_DISCLOSURE.md](AI_DISCLOSURE.md).

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Clojure 1.12](https://img.shields.io/badge/Clojure-1.12-blue.svg)](https://clojure.org)
[![CI](https://github.com/nurazhardotcom/tui/actions/workflows/ci.yml/badge.svg)](https://github.com/nurazhardotcom/tui/actions/workflows/ci.yml)

> Terminal-first AI agent harness in Clojure. Inspired by OpenCode architecture. Built ground-up in Clojure. Licensed under MIT.

> Demo: `vhs demo.tape` renders `demo.gif` locally (gitignored). See `demo.tape` for the scripted passkey + tool-loop walkthrough.

## Key Capabilities
- **Hybrid gateway (not a rewrite):** Clojure gateway handles passkey -> JWT -> Zen credential injection, then proxies to upstream `opencode web` as the backend. No forked UI, no vendored agent loop.
- **OIDC Passkey Gate:** embedded loopback (`localhost:8080`) + Clerk/Auth0/Keycloak WebAuthn, 8h scoped local JWT.
- **Zen Proxy (Responses API):** payload mapping to `POST https://opencode.ai/zen/v1/responses`, model `muse-spark-1.3-contributor-free`, key redaction in logs.
- **Deterministic gates:** allow-list tools, non-root enforcement, context budgeting, parallel streaming assembly.

> **Contributor-tier disclosure:** `muse-spark-1.3-contributor-free` is free in exchange for permission to use prompts/completions to train future Meta models. Bring your own key to opt out.

## Architecture
```
Browser/phone -> tui gateway :8080 (JWT gate, Zen inject, ****last4 logs)
                      |-> opencode web backend 127.0.0.1:4096 (sessions, PTY, tools)
                      |-> POST opencode.ai/zen/v1/responses (muse-spark-1.3-contributor-free)
                      | edge telemetry -> Cloudflare Worker (ingest + rollback target)
```

## Diagrams (learn visually)

Nine interactive diagrams in [`docs/archify/`](docs/archify/) — open the
`.html` files in a browser. Typed JSON IR → deterministic render
([archify method](https://github.com/tt-a1i/archify)); validated showcase,
zero warnings:

| Diagram | Question it answers |
|---|---|
| `runtime.html` | Phone → tunnel → gateway → opencode → Zen: where do raw keys go? |
| `namespaces.html` | What namespace does what, in what read order? |
| `request.html` | Per-request lifecycle incl. 401/503 exits? |
| `ci.html` | What runs per push, what blocks it? |
| `prompt.html` | One prompt to first streamed chunk, who calls whom? |
| `auth.html` | How does the JWT middleware accept/reject? |
| `secrets.html` | Raw secrets vs `****last4` — where's the boundary? |
| `context.html` | How is context budgeted to 32k? |
| `credential.html` | OAuth → expired → fallback → denied? |

Newcomer read order: namespaces → runtime → request → prompt → secrets → context → auth → credential → ci.

## Run it remotely (single-user, mobile-friendly)
```bash
# 1. on the host: start upstream backend
OPENCODE_SERVER_PASSWORD=secret opencode web --port 4096
# 2. start the gateway (reads ~/.config/tui/config.edn, needs TUI_JWT_SECRET)
TUI_JWT_SECRET=$(openssl rand -hex 32) clojure -M:run serve
# 3. expose via tunnel, open on your phone
cloudflared tunnel --url http://localhost:8080
```

## Quickstart
```bash
# prerequisites: Java 21+, clojure CLI
git clone https://github.com/nurazhardotcom/tui.git
cd tui
clojure -M:run
clojure -M:test   # parallel runner, expects 0 failures
```

Config `~/.config/tui/config.edn`:
```clojure
{:auth {:provider :clerk :challenge-url "https://auth.nurazhar.com/verify" :local-port 8080}
 :model {:provider-url "https://opencode.ai/zen/v1/responses" :model-name "muse-spark-1.3-contributor-free" :api-key-env "OPENCODE_ZEN_KEY"}
 :agent {:max-context-tokens 32000 :tool-timeout-ms 15000 :allowed-tools ["sh" "git" "cat" "ls"]}}
```

## Security & Isolation
1. Non-root only — `uid 0` traps the loop.
2. Tool calls require allow-list + schema gate.
3. Session JWTs expire in 8h, scoped to machine.
4. API keys redacted (`****last4`) in all telemetry.

## Credits
- OpenCode: execution-loop inspiration, no vendored code.
- Charm VHS: terminal demos.

## License
MIT — see `LICENSE`.
