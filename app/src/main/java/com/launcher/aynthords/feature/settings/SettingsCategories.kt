package com.launcher.aynthords.feature.settings

/**
 * Category and item definitions for the Settings UI.
 * Data only; no theme or LayoutSpec coupling.
 */
data class SettingsCategory(
    val id: String,
    val label: String,
    val items: List<SettingsItem>,
)

data class SettingsItem(
    val id: String,
    val label: String,
    val subtitle: String,
    val statusPill: String? = null,
    val isSubpage: Boolean = false,
)

object SettingsCategories {
    val all: List<SettingsCategory> = listOf(
        SettingsCategory(
            id = "appearance",
            label = "Appearance",
            items = listOf(
                SettingsItem("uiScale", "UI Scale", "Small / Default / Large", null),
                SettingsItem("gridColumns", "Grid columns", "Columns in game grid", null),
                SettingsItem("textSize", "Text size", "Adjust readable text size", null),
                SettingsItem("motion", "Motion", "Full / Reduced", null),
                SettingsItem("soundHaptics", "Sound & haptics", "UI clicks, navigation buzz", null),
                SettingsItem("theme", "Theme", "Affects home surfaces only", "default"),
            ),
        ),
        SettingsCategory(
            id = "homeLayout",
            label = "Home Layout",
            items = listOf(
                SettingsItem("settingsDisplay", "Settings display", "Top or bottom when both screens visible", null),
                SettingsItem("interactionSurface", "Interaction surface", "Top / Bottom", null),
                SettingsItem("presentationSurface", "Presentation surface", "Top / Bottom", null),
                SettingsItem("swapRoles", "Swap roles", "Swap Interaction and Presentation displays", null),
                SettingsItem("browseView", "Default browse view", "Grid / List", null),
            ),
        ),
        SettingsCategory(
            id = "library",
            label = "Library",
            items = listOf(
                SettingsItem("romDirectories", "ROM directories", "Choose folders for each platform", null),
                SettingsItem("libraryRefresh", "Library refresh", "Scan now / Auto-scan on boot", null),
                SettingsItem("ignoredFolders", "Ignored folders", "Exclude from scans", null),
                SettingsItem("mediaFolders", "Media folders", "Art and video locations", null),
            ),
        ),
        SettingsCategory(
            id = "emulation",
            label = "Emulation",
            items = listOf(
                SettingsItem("defaultEmulator", "Default emulator per platform", "Pick what launches your games", null),
                SettingsItem("launchArgs", "Per-platform launch args", "Advanced launch options", null),
            ),
        ),
        SettingsCategory(
            id = "online",
            label = "Online",
            items = listOf(
                SettingsItem("discord", "Discord integration", "Rich Presence toggle", "Off"),
                SettingsItem("scrapers", "Scrapers", "Download box art and metadata", null),
            ),
        ),
        SettingsCategory(
            id = "input",
            label = "Input",
            items = listOf(
                SettingsItem("controllerLayout", "Controller layout", "Preset, glyph set, customize bindings", null),
                SettingsItem("dpadNav", "DPAD navigation", "Wrap, scroll acceleration", null),
                SettingsItem("confirmLaunch", "Confirm before launching", "Require confirmation", null),
            ),
        ),
        SettingsCategory(
            id = "system",
            label = "System",
            items = listOf(
                SettingsItem("reassertDisplays", "Reassert displays", "Recover dual-screen layout", null),
                SettingsItem("storage", "Storage overview", "Free space and media location", null),
                SettingsItem("cache", "Cache management", "Clear art and metadata cache", null),
                SettingsItem("diagnostics", "Diagnostics", "Export logs, display state, selection ID", null),
            ),
        ),
        SettingsCategory(
            id = "about",
            label = "About",
            items = listOf(
                SettingsItem("version", "Version", "Build and device info", null),
                SettingsItem("licenses", "Licenses", "Open source licenses", null),
                SettingsItem("credits", "Credits", "Contributors", null),
                SettingsItem("reportBug", "Report a bug", "Exports logs and steps", null),
            ),
        ),
    )

    fun categoryById(id: String): SettingsCategory? = all.find { it.id == id }
    fun itemById(categoryId: String, itemId: String): SettingsItem? =
        categoryById(categoryId)?.items?.find { it.id == itemId }
}
