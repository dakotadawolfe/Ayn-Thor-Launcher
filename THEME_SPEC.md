# Theme Spec V1

Shareable theme files are JSON documents discovered from:

1. `app/src/main/assets/themes/*.json` (bundled defaults)
2. `<app internal files>/themes/*.json`
3. `<app external files>/themes/*.json`

The parser expects `schemaVersion: 1` and validates strict keys for metadata, palette, typography, motion, and layout surfaces.
If parsing or validation fails, the app safely falls back to `ThemeSpecDefaults.spec`.

Required metadata fields:
- `name`
- `version`
- `author`
- `compatibility` (`minSdk`, `minAppVersion`, optional `maxSdk`, `maxAppVersion`)
