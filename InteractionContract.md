# Interaction Contract

This document defines the explicit rules for user interaction within the Ayn Thor Launcher. All UI components and themes must adhere to these rules to ensure a consistent and predictable user experience.

## 1. Selection and Focus

*   **Rule 1.1: Focus Drives Selection.** For the initial implementation, the item that has DPAD focus is always considered the `selected` item. The `selectedGameId` in the `LauncherUiState` must update immediately as the focus moves.
*   **Rule 1.2: State Decoupling.** The `LauncherUiState` will maintain separate fields for `focusedGameId` and `selectedGameId`. While they will be kept in sync for now, this allows for future behaviors where selection might only change on an explicit "select" action (e.g., pressing the 'A' button).

## 2. Grid Navigation

*   **Rule 2.1: Edge Behavior is Clamp.** When navigating the game grid, reaching the edge of the grid in any direction will "clamp" the focus to that edge. The focus will not wrap around.
*   **Rule 2.2: Focus is Always Visible.** The grid must automatically scroll to ensure the currently focused item is always visible to the user.

## 3. Empty Library

*   **Rule 3.1: Graceful Empty State.** If the game library is empty, the Interaction Surface must display a clear, user-friendly message (e.g., "No games found. Go to Settings to import your library."). The Presentation Surface should display a default, non-game-specific background.
*   **Rule 3.2: No Null Selection on Boot.** If the library is not empty, a default item must be selected on application launch. The UI should never launch with a null selection state if content is available.
