# Ayn Thor Launcher Theme Authoring Guide

This document outlines how to create and customize themes for the Ayn Thor Launcher.

## Theme Structure

A theme is composed of two main parts:

1.  **Token Spec (`theme.json`):** Defines the visual style of the theme, including colors, typography, spacing, and motion.
2.  **Layout Spec (`layout.json`):** Defines the structure and arrangement of the UI elements on the screen.

## Token Spec (`theme.json`)

The `theme.json` file is the heart of your theme's visual identity. It contains a set of key-value pairs that define the color palette, typography, and other visual properties.

*(This section will be expanded with a full list of available tokens and their effects.)*

## Layout Spec (`layout.json`)

The `layout.json` file defines the composition of the launcher's UI using a palette of built-in "primitives." This allows you to radically alter the layout of the launcher without changing any code.

### Primitives

The following primitives are available for use in your layouts:

*   `GameGrid`
*   `Rail`
*   `HintBar`
*   `BackgroundArt`
*   `HeroArt`
*   `MetadataPanel`

### Example `layout.json`

```json
{
  "schemaVersion": 1,
  "layouts": {
    "INTERACTION": {
      "root": {
        "type": "Row",
        "children": [
          { "type": "Stack", "primitive": "Rail", "weight": 0.25 },
          {
            "type": "Column",
            "weight": 0.75,
            "children": [
              { "type": "Stack", "primitive": "GameGrid", "weight": 1 },
              { "type": "Stack", "primitive": "HintBar" }
            ]
          }
        ]
      }
    },
    "PRESENTATION": {
      "root": {
        "type": "Stack",
        "children": [
          { "type": "Stack", "primitive": "BackgroundArt" },
          { "type": "Stack", "primitive": "HeroArt" },
          { "type": "Stack", "primitive": "MetadataPanel", "params": { "alignment": "CenterEnd" } }
        ]
      }
    }
  }
}
```
