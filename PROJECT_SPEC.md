# AYN Thor Unified Dual-Screen Frontend — Project Spec

Purpose-Built System Shell (Not a Generic Launcher)

## Project Intent

Design and build a custom Android system shell exclusively for the AYN Thor, transforming it into a cohesive, intentional dual-screen gaming console.

- **Android is an implementation detail.** The user should never feel like they are "using Android."

This is **not**:

- a general-purpose launcher
- a Play Store product
- a theme slapped onto an Android app

This **is**:

- a Thor-specific home system
- a display-owning shell
- a console-grade UX surface

---

## Core Design Goals

- **Make the AYN Thor feel like a single, unified product**
  - No ambiguity about which screen does what
  - No "why did it move there?" moments
  - No recovery paths that require Android knowledge

- **Remove ambiguity around:**
  - which screen is the interaction surface
  - which screen is contextual/presentational
  - why navigation behaves the way it does

- **Support user preference without chaos**
  - Flexibility must be explicit and constrained
  - Defaults must be opinionated and predictable
  - Overrides must never create undefined states

- **Be developer-friendly and themeable from day one**
  - Themes are first-class citizens
  - Theme authors should not touch core logic
  - Radical visual/layout changes must be possible without forks

---

## Explicit Non-Goals (Hard Constraints)

- Not targeting other Android devices
- Not Play-Store-generic
- Not replicating desktop multi-monitor semantics
- Not relying on accessibility overlays or hacks as core UX
- Not modifying or merging third-party APKs (ES-DE, Companion, etc.)

**If a solution violates any of the above, it is rejected.**

---

## Hardware & OS Assumptions (Fixed)

- **Device:** AYN Thor only
- **OS:** Custom Android 13 build
- **Displays:** Two physical displays, both touch-enabled
- **HOME button:** System-owned, global (not per-display)

All design decisions must respect real Android multi-display behavior, not idealized or theoretical behavior.

---

## Dual-Screen Philosophy (Critical)

### Screen Roles Are Logical, Not Physical

There is no hardcoded "main" or "secondary" screen.

Users may prefer:

- UI on top, art/metadata on bottom
- UI on bottom, art/metadata on top
- Equal interaction on both screens

Therefore, the system must support:

- screen role switching
- UI ↔ presentation placement swapping
- touch interaction on both displays

The frontend defines roles, not hardware orientation.

### Display Roles (Logical Surfaces)

**Interaction Surface**

- Primary navigation
- Game selection
- Input-heavy UI
- Owns focus and selection state

**Presentation Surface**

- Fan art, box art, video, metadata
- Dashboards, companions, social context
- May be interactive, but does not own selection

Roles are dynamically assignable and explicitly modeled.

---

## High-Level Architecture

### Single APK, Multi-Activity Shell

One APK acts as:

- the default HOME app
- the display owner for both screens
- the authority for screen roles and layout state

### Activities

- **Exactly two host activities**
  - `InteractionHostActivity` (HOME entrypoint)
  - `PresentationHostActivity` (secondary surface)
- One activity per display
- **Activities are thin hosts only** — they do not contain UI logic

### Core Invariant

**Display/session logic must never live inside composables.**  
UI renders state; controllers mutate state.

### Launcher Behavior

- App is the default HOME
- HOME always returns to the Interaction surface
- On resume: display roles are reasserted; missing secondary display is recovered
- Recovery paths are visible and intentional
- The user should never need to "understand Android" to recover state.

### Navigation Philosophy

- Do not rely on the system HOME button for per-screen behavior
- Navigation lives inside the frontend UI
- **Predictability > cleverness**
- No silent behavior; no dead ends
- If a choice must be made: favor what a human expects from a dual-screen gaming console, not what Android technically prefers.

---

## Theming & Customization (Developer-First)

### Theme System Goals

- Themes are first-class, not an afterthought
- Theme authors can radically alter layout and visuals
- Core logic remains untouched

### Theme Architecture (Mandatory)

**Theme = Tokens + Composition**

**TokenSpec**

- Colors, typography, spacing, motion, shapes
- Stable, versioned, forward-compatible

**LayoutSpec**

- Declarative composition graph
- Built from a fixed palette of primitives
- Controls layout per surface role
- Themes arrange primitives; they do not own logic

**Primitive Palette (Example)**

- GameGrid / GameList
- Rail / Dock
- HintBar
- BackgroundArt
- HeroArt
- MetadataPanel
- SelectionIndicator

Keep the palette small and stable.

**Future Extension (Planned, Not Required)**  
Theme Packs may later provide custom composables via a stable interface, but data-driven theming is the default.

---

## UX Principles (Non-Negotiable)

- Opinionated defaults, flexible overrides
- Predictability beats cleverness
- No silent state changes
- No "why did it do that?" moments
- Visual feedback always beats invisible logic

---

## Technical Preferences

- Kotlin
- Jetpack Compose (Compose-first UI)
- Explicit state management
- Minimal permissions
- No hidden APIs
- No accessibility hacks as core functionality
- Foreground services only when unavoidable

---

## End Vision

The final product should feel like:

**"This is how the AYN Thor was meant to work."**

Not: Android with layers, multiple launchers, companion apps fighting for focus.

But: a purpose-built dual-screen console shell with intentional layout, clear ownership of screens, and deep customization without chaos.

### Desired Outcomes

- Clear system architecture
- Explicit display-role state machine
- Theme system designed before UI complexity
- A frontend that makes Android invisible to the user

---

## Meta-Guidance for Agents

- Always assess current state before editing
- Never speculate when a deterministic explanation exists
- Do not "fix" by thrashing manifests or activities
- Prefer boring, explicit systems over clever shortcuts
- If UI is blank, verify rendering paths before changing themes or logic
