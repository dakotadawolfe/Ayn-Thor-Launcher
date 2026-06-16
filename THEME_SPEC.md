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

---

## Tokens: What Each Value Influences

*(This section should be expanded to detail every token available in the `palette`, `typography`, `motion`, and `layout` sections of the theme JSON.)*

**Example:**
*   `palette.primary.main`: The core brand color, used for focused items, primary buttons, etc.
*   `motion.duration.fast`: Used for quick, responsive animations like button presses.
*   `layout.spacing.grid`: The standard gutter size for items in a grid.

---

## LayoutSpec: Available Primitives + Parameters

*This is the core of the data-driven layout system. It defines the building blocks that themes can arrange. The palette is kept small and stable (see `LayoutSpec.kt` and AGENTS.md).*

**Implemented Primitives (stable palette):**

*   `GameGrid`: A grid of game art.
    *   Parameters: `columns` (int), `itemSpacing` (dp), `selectionStyle` (enum: `border`, `glow`, `scale`).
*   `Rail`: A vertical rail or dock for navigation (e.g. collection list).
*   `HintBar`: A bar showing contextual hints (e.g. "A = Launch | B = Back").
*   `BackgroundArt`: Background image/gradient for the selected game.
*   `HeroArt`: A large, prominent display of the selected game's art.
    *   Parameters: `blurRadius` (dp), `saturation` (float).
*   `MetadataPanel`: Displays information about the selected game.
    *   Parameters: `showTitle` (bool), `showLastPlayed` (bool), `alignment` (e.g. `CenterEnd` in Stack).
*   `SelectionIndicator`: Visual indicator for the current focus/selection (e.g. badge or highlight).

**Example LayoutSpec:**

```json
"layoutSpec": {
  "interaction": {
    "type": "Row",
    "children": [
      { "primitive": "GameGrid", "weight": 0.7 },
      { "primitive": "MetadataPanel", "weight": 0.3 }
    ]
  },
  "presentation": {
    "type": "Stack",
    "children": [
      { "primitive": "Background", "params": { "source": "imageUrl", "urlKey": "selectedGame.art.hero" } },
      { "primitive": "HeroArt" }
    ]
  }
}
```

---

## Selection Model: How Focus is Represented

*The launcher maintains a single, unified selection state (`LauncherUiState.selectedGameId`). Themes do not manage this state, they only render it.*

*   **Interaction Surface:** Must clearly indicate the currently focused item. This is the primary driver of selection.
*   **Presentation Surface:** Reacts to the selection from the interaction surface, typically by updating the `HeroArt` and `MetadataPanel`.
*   Themes can customize the *visual representation* of selection (e.g., via `GameGrid.selectionStyle`) but not the logic.

---

## Asset Pipeline: What URLs/Keys are Expected

*Themes do not fetch assets directly. They reference asset keys provided by the application's state models. This allows the backend to change without breaking themes.*

**Expected Keys:**

*   `selectedGame.art.hero`: URL for the main hero image.
*   `selectedGame.art.box`: URL for the box art.
*   `selectedGame.art.background`: URL for the background image.
*   `collection.icon`: URL for a collection's icon.

*The app is responsible for loading these URLs (e.g., via Coil) and handling caching and placeholders.*

---

## Performance Budget Guidelines

*A beautiful theme is useless if it's not performant. Adhering to these guidelines is crucial for a smooth, "buttery" experience.*

*   **Composition:** Avoid deep or excessively complex composable hierarchies.
*   **Overdraw:** Use `Modifier.background()` responsibly. Avoid layering multiple opaque backgrounds.
*   **Animation:** Prefer simple, hardware-accelerated animations (translation, rotation, scale, alpha). Use `animate*AsState` and `Animatable` for most cases. Avoid complex, state-driven animations that can cause excessive recomposition.
*   **Image Loading:** All image loading is handled by the app. Do not attempt to load images directly in a theme.
*   **LayoutSpec:** Be mindful of the performance cost of certain primitives. A `GameGrid` with hundreds of items will be more demanding than a simple `GameList`.
