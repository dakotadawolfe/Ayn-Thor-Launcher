package com.launcher.aynthords.input

/**
 * Semantic app actions. UI and logic reference Actions only—never physical buttons.
 * Mapping layer translates hardware → Action.
 */
enum class Action {
    Confirm,
    Back,
    Menu,
    Search,
    Context,
    PageLeft,
    PageRight,
    QuickSwapDisplays,
    Filter,
    FavoritesToggle,
    NavUp,
    NavDown,
    NavLeft,
    NavRight,
    Details,
    ResetCategory,
}
