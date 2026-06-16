package com.launcher.aynthords.feature.settings

import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.launcher.aynthords.shell.display.DualScreenSessionController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.launcher.aynthords.BuildConfig
import com.launcher.aynthords.feature.launcher.LauncherIntent
import com.launcher.aynthords.feature.launcher.LauncherStore
import com.launcher.aynthords.shell.HomeRoleHelper
import com.launcher.aynthords.shell.display.DisplayRoleStore
import com.launcher.aynthords.shell.display.PhysicalSurface
import com.launcher.aynthords.shell.display.performUserSwap
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime
import com.launcher.aynthords.input.Action
import com.launcher.aynthords.input.ActionDispatcher

/**
 * In-shell Settings overlay. Console aesthetic: flat, structured, high contrast.
 * Two-column layout: left rail + right content. B = close, Y = reset category.
 */
@Composable
fun InShellSettingsOverlay(
    displayId: Int,
    onIntent: (LauncherIntent) -> Unit,
    onReassert: () -> Unit,
    onSetAsDefaultLauncher: (() -> Unit)? = null,
) {
    val theme = LocalThemeRuntime.current
    val structure = LocalSettingsStructure.current
    val context = LocalContext.current
    val settingsState by SettingsStore.state.collectAsState()
    val launcherState by LauncherStore.state.collectAsState()
    val isDefaultHome = HomeRoleHelper.isDefaultHome(context)
    val roleState by DisplayRoleStore.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(structure.baseBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 48.dp),
        ) {
            // Left rail: darker, fixed width, no wrapping
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .background(structure.railBg)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                SettingsCategories.all.forEachIndexed { index, category ->
                    val isSelected = settingsState.selectedCategoryId == category.id
                    val isFocused = !settingsState.focusInItems && index == settingsState.focusedCategoryIndex
                    SettingsCategoryRow(
                        label = category.label,
                        isSelected = isSelected,
                        isFocused = isFocused,
                        accentColor = structure.accentColor,
                        railBg = structure.railBg,
                        onClick = {
                            if (isSelected) {
                                SettingsStore.focusOnRail()
                            } else {
                                SettingsStore.selectCategory(category.id)
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.width(1.dp))

            // Right pane: slightly lighter content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(structure.contentBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                when {
                    settingsState.selectedSubpageId == "diagnostics" ->
                        DiagnosticsContent(onDismiss = { SettingsStore.selectSubpage(null) })
                    settingsState.selectedSubpageId == "controllerLayout" ->
                        ControllerLayoutContent(
                            onDismiss = { SettingsStore.selectSubpage(null) },
                            onOpenCustomize = { SettingsStore.selectSubpage("customizeControls") },
                        )
                    settingsState.selectedSubpageId == "customizeControls" ->
                        CustomizeControlsContent(onDismiss = { SettingsStore.selectSubpage("controllerLayout") })
                    else -> {
                        val catId = settingsState.selectedCategoryId ?: SettingsCategories.all.first().id
                        val category = SettingsCategories.categoryById(catId)
                        if (category != null) {
                            SettingsRightPane(
                                category = category,
                                focusedItemIndex = settingsState.focusedItemIndex,
                                focusInItems = settingsState.focusInItems,
                                launcherState = launcherState,
                                roleState = roleState,
                                displayId = displayId,
                                isDefaultHome = isDefaultHome,
                                onIntent = onIntent,
                                onReassert = onReassert,
                                onSetAsDefaultLauncher = onSetAsDefaultLauncher,
                                onItemClick = { catId, itemId ->
                                    if (itemId == "diagnostics") {
                                        SettingsStore.selectSubpage("diagnostics")
                                    } else if (itemId == "controllerLayout") {
                                        SettingsStore.selectSubpage("controllerLayout")
                                    } else {
                                        handleItemAction(
                                            context = context,
                                            categoryId = catId,
                                            itemId = itemId,
                                            displayId = displayId,
                                            onIntent = onIntent,
                                            onReassert = onReassert,
                                            onSetAsDefaultLauncher = onSetAsDefaultLauncher,
                                            isDefaultHome = isDefaultHome,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        // Hint bar: anchored bottom, darker strip, controller glyphs
        HintBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SettingsCategoryRow(
    label: String,
    isSelected: Boolean,
    isFocused: Boolean,
    accentColor: Color,
    railBg: Color,
    onClick: () -> Unit,
) {
    val theme = LocalThemeRuntime.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .focusable()
            .then(if (isFocused) Modifier.border(2.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp)) else Modifier)
            .drawBehind {
                if (isSelected) {
                    drawRect(
                        color = accentColor,
                        topLeft = Offset.Zero,
                        size = Size(4.dp.toPx(), size.height)
                    )
                }
            }
            .background(
                when {
                    isSelected -> theme.surfaceVariant.copy(alpha = if (isFocused) 0.2f else 0.15f)
                    isFocused -> theme.surfaceVariant.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (isSelected || isFocused) theme.onSurface else theme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SettingsRightPane(
    category: SettingsCategory,
    focusedItemIndex: Int,
    focusInItems: Boolean,
    launcherState: com.launcher.aynthords.feature.launcher.LauncherUiState,
    roleState: com.launcher.aynthords.shell.display.DisplayRoleState,
    displayId: Int,
    isDefaultHome: Boolean,
    onIntent: (LauncherIntent) -> Unit,
    onReassert: () -> Unit,
    onSetAsDefaultLauncher: (() -> Unit)?,
    onItemClick: (String, String) -> Unit,
) {
    val theme = LocalThemeRuntime.current
    val listState = rememberLazyListState()
    val itemCount = category.items.size + (if (category.id == "system" && !isDefaultHome && onSetAsDefaultLauncher != null) 1 else 0)

    LaunchedEffect(focusedItemIndex, focusInItems) {
        if (focusInItems && focusedItemIndex in 0 until itemCount) {
            val scrollIndex = 1 + 2 * focusedItemIndex
            listState.animateScrollToItem(scrollIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = category.label,
                color = theme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        category.items.forEachIndexed { index, item ->
            item(key = "item_$index") {
                val statusPill = resolveStatusPill(item, launcherState, roleState)
                val isFocused = focusInItems && index == focusedItemIndex
                SettingsItemRow(
                    label = item.label,
                    subtitle = item.subtitle,
                    statusPill = statusPill ?: item.statusPill,
                    isFocused = isFocused,
                    onClick = { onItemClick(category.id, item.id) },
                )
            }
            if (index < category.items.lastIndex) {
                item(key = "div_$index") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(theme.surfaceVariant.copy(alpha = 0.2f))
                    )
                }
            }
        }
        if (category.id == "system" && !isDefaultHome && onSetAsDefaultLauncher != null) {
            item(key = "div_system") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(theme.surfaceVariant.copy(alpha = 0.2f))
                )
            }
            item(key = "set_default") {
                SettingsItemRow(
                    label = "Set as default launcher",
                    subtitle = "HOME will open Ayn Thor Launcher",
                    statusPill = null,
                    isFocused = focusInItems && focusedItemIndex == category.items.size,
                    onClick = { onSetAsDefaultLauncher() },
                )
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    label: String,
    subtitle: String,
    statusPill: String?,
    isFocused: Boolean,
    onClick: () -> Unit,
) {
    val theme = LocalThemeRuntime.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .focusable()
            .background(if (isFocused) theme.surfaceVariant.copy(alpha = 0.08f) else Color.Transparent)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = theme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = theme.onSurfaceVariant.copy(alpha = 0.65f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (statusPill != null) {
            Text(
                text = statusPill,
                color = theme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun HintBar(modifier: Modifier = Modifier) {
    val theme = LocalThemeRuntime.current
    val structure = LocalSettingsStructure.current
    val config by com.launcher.aynthords.input.InputConfig.state.collectAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(structure.baseBg.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        com.launcher.aynthords.input.ActionGlyph(
            action = com.launcher.aynthords.input.Action.Confirm,
            config = config,
            size = 24.dp,
            backgroundColor = theme.surfaceVariant.copy(alpha = 0.4f),
            textColor = theme.onSurfaceVariant,
        )
        Text("Select", color = theme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 11.sp)
        com.launcher.aynthords.input.ActionGlyph(
            action = com.launcher.aynthords.input.Action.Back,
            config = config,
            size = 24.dp,
            backgroundColor = theme.surfaceVariant.copy(alpha = 0.4f),
            textColor = theme.onSurfaceVariant,
        )
        Text("Back", color = theme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 11.sp)
        com.launcher.aynthords.input.ActionGlyph(
            action = com.launcher.aynthords.input.Action.ResetCategory,
            config = config,
            size = 24.dp,
            backgroundColor = theme.surfaceVariant.copy(alpha = 0.4f),
            textColor = theme.onSurfaceVariant,
        )
        Text("Reset", color = theme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

private fun resolveStatusPill(
    item: SettingsItem,
    launcherState: com.launcher.aynthords.feature.launcher.LauncherUiState,
    roleState: com.launcher.aynthords.shell.display.DisplayRoleState,
): String? = when (item.id) {
    "uiScale" -> "${"%.0f".format(launcherState.uiDensityMultiplier * 100)}%"
    "gridColumns" -> "${launcherState.gridColumns}"
    "theme" -> "default"
    "settingsDisplay" -> launcherState.settingsDisplayWhenDual.name.lowercase().replaceFirstChar { it.uppercase() }
    "interactionSurface", "presentationSurface" -> {
        val mapping = roleState.currentMapping
        when (item.id) {
            "interactionSurface" -> if (mapping.top == com.launcher.aynthords.domain.model.SurfaceRole.INTERACTION) "Top" else "Bottom"
            "presentationSurface" -> if (mapping.top == com.launcher.aynthords.domain.model.SurfaceRole.PRESENTATION) "Top" else "Bottom"
            else -> null
        }
    }
    else -> null
}

private fun handleItemAction(
    context: android.content.Context,
    categoryId: String,
    itemId: String,
    displayId: Int,
    onIntent: (LauncherIntent) -> Unit,
    onReassert: () -> Unit,
    onSetAsDefaultLauncher: (() -> Unit)?,
    isDefaultHome: Boolean,
) {
    when (categoryId to itemId) {
        "appearance" to "gridColumns" -> {
            val current = LauncherStore.state.value.gridColumns
            val next = when (current) {
                2 -> 3
                3 -> 4
                4 -> 5
                5 -> 6
                else -> 2
            }
            onIntent(LauncherIntent.SetGridColumns(next))
        }
        "appearance" to "uiScale" -> {
            val current = LauncherStore.state.value.uiDensityMultiplier
            val next = when {
                current <= 0.9f -> 1f
                current <= 1.05f -> 1.15f
                else -> 0.85f
            }
            onIntent(LauncherIntent.SetUiDensity(next))
        }
        "appearance" to "theme",
        "appearance" to "textSize",
        "appearance" to "motion",
        "appearance" to "soundHaptics",
        "library" to "romDirectories",
        "library" to "ignoredFolders",
        "library" to "mediaFolders",
        "emulation" to "defaultEmulator",
        "emulation" to "launchArgs",
        "online" to "discord",
        "online" to "scrapers",
        "input" to "controllerLayout" -> SettingsStore.selectSubpage("controllerLayout")
        "input" to "dpadNav",
        "input" to "confirmLaunch",
        "system" to "reassertDisplays" -> { onReassert() }
        "system" to "setDefaultLauncher" -> { onSetAsDefaultLauncher?.invoke() }
        "system" to "storage",
        "system" to "cache",
        "about" to "licenses",
        "about" to "credits",
        "about" to "reportBug" -> Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        "homeLayout" to "settingsDisplay" -> {
            val current = LauncherStore.state.value.settingsDisplayWhenDual
            val next = if (current == PhysicalSurface.TOP) PhysicalSurface.BOTTOM else PhysicalSurface.TOP
            onIntent(LauncherIntent.SetSettingsDisplayWhenDual(next))
        }
        "homeLayout" to "swapRoles", "homeLayout" to "interactionSurface", "homeLayout" to "presentationSurface" -> {
            performUserSwap(context, displayId)
            onReassert()
        }
        "homeLayout" to "browseView" -> Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        "library" to "libraryRefresh" -> {
            com.launcher.aynthords.library.LibraryRefreshController.requestRefresh()
            Toast.makeText(context, "Library refresh requested", Toast.LENGTH_SHORT).show()
        }
        "about" to "version" -> Toast.makeText(
            context,
            "Ayn Thor Launcher ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            Toast.LENGTH_LONG
        ).show()
        else -> {}
    }
}

/**
 * Handles DPAD and A/Enter when settings overlay is open.
 * Call from host activity dispatchKeyEvent before super.
 */
fun processSettingsKeyEvent(activity: ComponentActivity, keyCode: Int): Boolean {
    if (!LauncherStore.state.value.settingsOpen) return false
    val onIntent: (LauncherIntent) -> Unit = { LauncherStore.dispatch(it, activity) }
    val onReassert: () -> Unit = { DualScreenSessionController(activity).reassert() }
    val onSetAsDefaultLauncher: (() -> Unit)? = { HomeRoleHelper.launchRequestHomeRole(activity) }
    val isDefaultHome = HomeRoleHelper.isDefaultHome(activity)
    val displayId = ContextCompat.getDisplayOrDefault(activity).displayId

    val nav = ActionDispatcher.resolveNav(keyCode)
    if (nav != null) {
        return when (nav) {
            com.launcher.aynthords.input.NavDirection.Up -> SettingsStore.handleDpad(SettingsStore.DpadDirection.Up, isDefaultHome)
            com.launcher.aynthords.input.NavDirection.Down -> SettingsStore.handleDpad(SettingsStore.DpadDirection.Down, isDefaultHome)
            com.launcher.aynthords.input.NavDirection.Left -> SettingsStore.handleDpad(SettingsStore.DpadDirection.Left, isDefaultHome)
            com.launcher.aynthords.input.NavDirection.Right -> SettingsStore.handleDpad(SettingsStore.DpadDirection.Right, isDefaultHome)
        }
    }
    when {
        ActionDispatcher.resolveAction(keyCode) == Action.Confirm -> {
            val cat = SettingsStore.getFocusedCategoryForActivation()
            if (cat != null) {
                SettingsStore.selectCategory(cat)
                return true
            }
            val pair = SettingsStore.getFocusedItemAction(isDefaultHome)
            if (pair != null) {
                handleItemAction(
                    context = activity,
                    categoryId = pair.first,
                    itemId = pair.second,
                    displayId = displayId,
                    onIntent = onIntent,
                    onReassert = onReassert,
                    onSetAsDefaultLauncher = onSetAsDefaultLauncher,
                    isDefaultHome = isDefaultHome,
                )
                return true
            }
            return false
        }
        else -> return false
    }
}
