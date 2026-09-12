# tui — TUI `tui.nurazhar.com`

[![License: AGPL v3](https://img.shields.io/badge/License-AGPLv3-blue.svg)](LICENSE)
[![Clojure 1.12](https://img.shields.io/badge/Clojure-1.12-blue.svg)](https://clojure.org)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

> Terminal-first AI agent harness in Clojure. Inspired by OpenCode architecture. Built ground-up in Clojure. Licensed under GNU AGPLv3.

![Demo](./demo.gif)

## Key Capabilities
- **Decoupled Engine/Harness:** local tool execution separate from model reasoning and identity verification.
- **OIDC Passkey Gate:** embedded loopback (`localhost:8080`) + Clerk/Auth0/Keycloak WebAuthn, 8h scoped local JWT.
- **Zen Proxy:** OpenAI-compatible payload mapping to `https://opencode.ai/zen/v1`, key redaction in logs.
- **Deterministic gates:** allow-list tools, non-root enforcement, context budgeting, parallel streaming assembly.

## Architecture
```
TUI CLI -> localhost:8080/auth -> OIDC Passkey -> local JWT -> Zen API adapter -> opencode.ai/zen/v1
                 | edge telemetry -> Cloudflare Worker (ingest + rollback target)
```

## Quickstart
```bash
# prerequisites: Java 21+, clojure CLI
git clone https://github.com/nurazhardotcom/tui.git
cd tui
clj -M:run
clj -M:test   # parallel runner, expects 0 failures
```

Config `~/.config/tui/config.edn`:
```clojure
{:auth {:provider :clerk :challenge-url "https://auth.nurazhar.com/verify" :local-port 8080}
 :model {:provider-url "https://opencode.ai/zen/v1" :model-name "opencode-zen-free" :api-key-env "OPENCODE_ZEN_KEY"}
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
GNU AGPLv3 — see `LICENSE`. Network use triggers source disclosure (§13).
