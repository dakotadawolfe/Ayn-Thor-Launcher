# AYN Thor Launcher

AYN Thor Launcher is a Kotlin and Jetpack Compose Android shell built specifically for the AYN Thor dual-screen handheld. It is not a generic Android launcher. The project models the device as a purpose-built gaming console with two logical surfaces: an interaction surface for navigation and a presentation surface for art, metadata, and secondary context.

The launcher owns HOME behavior, tracks display-role mapping, recovers missing secondary surfaces, and keeps themes as data-driven specifications rather than hardcoded UI forks.

## Status

This is an active device-specific prototype. It assumes the AYN Thor hardware, Android 13 behavior, two touch displays, and a controlled installation environment. It is not intended as a Play Store launcher for arbitrary Android devices.

## Features

- Dual-host architecture with `InteractionHostActivity` and `PresentationHostActivity`.
- Logical display roles instead of hardcoded top/bottom screen assumptions.
- HOME reassertion and secondary-display recovery paths.
- Kotlin/Compose launcher UI with grid navigation, details, settings, and presentation surfaces.
- Local app library scanning, curation, favorites, hidden apps, collections, and launch tracking.
- Theme system based on versioned tokens and layout specs.
- Built-in default theme assets under `app/src/main/assets/themes/`.
- Device-owner and secondary-home helper utilities for controlled shell behavior.
- Debug logging to app-local files for display and recovery diagnostics.

## Requirements

- Android Studio or a local Android Gradle toolchain.
- JDK 11 or newer.
- Android SDK with compile SDK 34.
- ADB for device install.
- AYN Thor device or emulator/device setup that can expose the required display behavior.

## Build

Windows:

```bat
gradlew.bat assembleDebug
```

Convenience wrapper:

```bat
build-apk.cmd
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install

With a connected device:

```bat
gradlew.bat installDebug
```

or:

```bat
install-debug.cmd
```

## Project Layout

```text
.
|-- app/                         # Android app module
|-- app/src/main/assets/themes/   # Default theme specs
|-- app/src/main/assets/platforms # Platform metadata
|-- PROJECT_SPEC.md              # Product and architecture intent
|-- InteractionContract.md        # Focus, selection, and navigation rules
|-- THEMES.md                    # Theme authoring overview
|-- THEME_SPEC.md                # Versioned token/layout specification
|-- build-apk.cmd                # Debug APK build helper
`-- install-debug.cmd            # Debug install helper
```

Important code areas:

- `shell/display/`: display discovery, role mapping, launch/recovery, and session controllers.
- `feature/launcher/`: interaction and presentation UI state.
- `feature/settings/`: in-shell settings and diagnostics.
- `theme/`: theme spec, compatibility checks, repository, and runtime conversion.
- `input/`: physical input mapping, action dispatch, glyphs, and presets.
- `data/local/`: local library, artwork, profile, and curation storage.

## Architecture

The launcher uses two logical roles:

- `INTERACTION`: primary navigation and focus owner.
- `PRESENTATION`: artwork, metadata, dashboards, and secondary context.

Physical displays are mapped to roles at runtime. This lets the shell support user preference and recovery without scattering display assumptions through composables.

Core rule:

```text
Display/session logic lives in controllers and stores. UI renders state.
```

## Theming

Themes are defined as data:

- `theme.json` controls colors, typography, spacing, shapes, and motion.
- `layout.json` controls the composition graph for interaction and presentation surfaces.

Built-in primitives include:

- `GameGrid`
- `Rail`
- `HintBar`
- `BackgroundArt`
- `HeroArt`
- `MetadataPanel`
- `SelectionIndicator`

See `THEMES.md` and `THEME_SPEC.md` before adding or changing theme packs.

## Tests

Run unit tests:

```bat
gradlew.bat test
```

Run Android instrumentation tests from Android Studio or with a connected device:

```bat
gradlew.bat connectedAndroidTest
```

## Development Notes

- Keep AYN Thor assumptions explicit.
- Do not move display/session state into composables.
- Prefer deterministic recovery flows over silent Android fallback behavior.
- Treat `PROJECT_SPEC.md` and `InteractionContract.md` as design constraints.
- If a secondary display is missing or blank, inspect display registry and session-controller logs before changing UI layout.
