# autocode experiment — merged notes

**Status:** experiment retired. Live repo archived at
[`nurazhardotcom/autocode`](https://github.com/nurazhardotcom/autocode)
(read-only). These notes preserve what was learned.

## Lineage

`autocode` was a Clojure-native reimagining of
[`empero-org/autocode`](https://github.com/empero-org/autocode)
(Apache-2.0; upstream attribution retained in that repo's `NOTICE` per
Apache-2.0 §4(d)): a minimal self-mutating coding agent — one agent loop,
one built-in tool (`bash`), zero third-party dependencies (Clojure core +
JDK only), EDN session transcripts, file-based tools, homoiconic
self-modification.

## Ideas tested

1. **Zero-dep loop.** Agent loop, streaming `/chat/completions` client,
   sessions, compaction, and REPL in ~7 namespaces using only
   `clojure.core` and the JDK (`java.net.http`, `ProcessBuilder`).
2. **One tool + agent-written tools.** `bash` runs as written (persistent
   cwd, closed stdin, timeout); any `.autocode/tools/<name>.clj` defining
   a `SCHEMA` map and `(run args-map)` is picked up next step. Broken
   tools surface as `BROKEN` with their error so the agent can fix them.
3. **Self-modification.** Before each model call the agent checks whether
   its own sources changed; if they still parse it backs up and relaunches
   into the same turn, else the syntax error feeds back and the current
   version keeps running (`--diff` / `--reset` included).
4. **Compaction.** Past `compact_at × context_window` (or on server
   length-rejection) the model summarizes; the summary replaces everything
   except the newest message and latest step. Full transcripts kept under
   `.autocode/sessions/old/`.
5. **Layered config.** Built-in defaults < `~/.config` < project config <
   `AUTOCODE_*` env < CLI flags; API keys as `$VAR` references, never in
   files.

## Adopted vs rejected by tui

| Idea | Verdict |
|---|---|
| EDN session transcripts, clipped tool output for context budget | **Adopted** — same budgeting philosophy as `prune-context` / `sanitize-output` |
| Layered config with `$VAR` key references | **Adopted** — see `load-config` + `api-key-env` |
| File-defined tools with schemas | **Adopted in spirit** — tui keeps the stricter allow-list + schema gate instead |
| Self-modifying agent | **Rejected** — incompatible with a gateway that must stay auditable; tui never rewrites itself |
| Unsandboxed `bash`-as-written | **Rejected** — tui requires allow-list + non-root + human-operated origin |

## Why it retired

Two public agent harnesses told a scattered story. The gateway
(`tui.gateway`) plus upstream `opencode web` covers the product surface;
`autocode`'s value was the five ideas above, now recorded here. No code
was ported verbatim — this is a lineage note, not a vendoring.
