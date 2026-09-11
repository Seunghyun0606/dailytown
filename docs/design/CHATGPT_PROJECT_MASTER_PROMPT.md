# Deprecated compatibility path

This file is kept temporarily because existing ChatGPT Project instructions may still reference it.

The design-session rules have moved to:

`/docs/design/DESIGN_SESSION_INSTRUCTIONS.md`

For any Daily Town design task, read and follow that file together with the design-system documents referenced from `/docs/design/README.md`.

Do not treat this compatibility file as a separate or competing source of design rules.

## Binary asset compatibility note

Binary production assets are not complete merely because an image was generated or previewed in chat. Daily Town PNG/JPG/WebP/sprite outputs must follow the binary persistence and remote-verification rules in `/docs/design/DESIGN_SESSION_INSTRUCTIONS.md`.

When a ChatGPT Work environment provides `@ai-remote-viral-pjt-01`, use that binary-capable repository workspace for materialization and Git commit/push rather than attempting to transmit binary bytes through text-only GitHub file create/update operations. If remote persistence cannot be verified, report the asset as `NOT PERSISTED` rather than claiming completion.
