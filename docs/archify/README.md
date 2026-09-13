# docs/archify — tui diagrams

Nine interactive diagrams for learning the codebase visually. Method:
[tt-a1i/archify](https://github.com/tt-a1i/archify) — typed JSON IR in this
folder, deterministically compiled to self-contained HTML (no build step,
works offline, dark/light toggle with `T`, `/` to search).

Open any `.html` directly in a browser (or `python3 -m http.server` here).

| # | Diagram | Type | Answers |
|---|---|---|---|
| 1 | [runtime.html](runtime.html) ([IR](runtime.architecture.json)) | architecture | How does a prompt travel phone → tunnel → gateway → opencode → Zen? Where may raw keys go? |
| 2 | [namespaces.html](namespaces.html) ([IR](namespaces.architecture.json)) | architecture | What namespace does what, and what order do I read them in? |
| 3 | [request.html](request.html) ([IR](request.workflow.json)) | workflow | What happens step by step per request, including 401/503 exits? |
| 4 | [ci.html](ci.html) ([IR](ci.workflow.json)) | workflow | What runs on every push, and what blocks it? |
| 5 | [prompt.html](prompt.html) ([IR](prompt.sequence.json)) | sequence | One prompt to first streamed chunk — who calls whom, in what order? |
| 6 | [auth.html](auth.html) ([IR](auth.sequence.json)) | sequence | How does the JWT middleware accept or reject? |
| 7 | [secrets.html](secrets.html) ([IR](secrets.dataflow.json)) | dataflow | Where do raw secrets flow vs `****last4` logs? |
| 8 | [context.html](context.html) ([IR](context.dataflow.json)) | dataflow | How is context budgeted to 32k before the model sees it? |
| 9 | [credential.html](credential.html) ([IR](credential.lifecycle.json)) | lifecycle | OAuth → expired → fallback → denied: how does credential state evolve? |

## Evidence (2026-09-13)

- `validate … --quality showcase`: **9/9 pass, 0 errors, 0 warnings.**
- `deliver`: **9/9 artifacts** written deterministically (SHA-256 in receipts).
- `visual-check` (automated Chrome): containment **5/9 pass**
  (runtime, namespaces, ci, secrets, context). `request`, `prompt`,
  `auth`, `credential` overflow vertically at desktop viewports — fully
  readable with scroll, no horizontal overflow. Perceptual visual review:
  **pending (human)**.
- Read order for newcomers: 2 → 1 → 3 → 5 → 7 → 8 → 6 → 9 → 4.
