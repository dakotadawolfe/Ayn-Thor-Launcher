package com.launcher.aynthords.feature.launcher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.launcher.aynthords.ui.theme.toMaterialColorScheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.request.ImageRequest
import com.launcher.aynthords.domain.model.PresentationLoadingState
import com.launcher.aynthords.shell.launch.LaunchFailure
import com.launcher.aynthords.domain.model.PresentationUiState
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.shell.display.DisplayRegistry
import com.launcher.aynthords.shell.display.PhysicalSurface
import com.launcher.aynthords.theme.ThemeRepository
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime
import com.launcher.aynthords.theme.runtime.ThemeRuntime
import com.launcher.aynthords.theme.spec.LayoutNode
import com.launcher.aynthords.theme.spec.LayoutSpecV1
import com.launcher.aynthords.theme.spec.NodeType
import com.launcher.aynthords.theme.spec.Primitive
import com.launcher.aynthords.theme.spec.ExtraElement
import com.launcher.aynthords.theme.spec.ExtraElementType
import com.launcher.aynthords.theme.spec.SurfaceLayoutSpec
import com.launcher.aynthords.theme.spec.ThemeSpecV1
import com.launcher.aynthords.feature.details.DetailsScreen
import com.launcher.aynthords.feature.settings.InShellSettingsOverlay
import com.launcher.aynthords.feature.settings.SystemSettingsTheme
import com.launcher.aynthords.data.local.ArtworkType
import com.launcher.aynthords.shell.HomeRoleHelper

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThorRoot(
    surfaceRole: SurfaceRole,
    displayId: Int,
    onIntent: (LauncherIntent) -> Unit = { LauncherStore.dispatch(it) },
    onReassert: (() -> Unit)? = null,
    onSetAsDefaultLauncher: (() -> Unit)? = null,
) {
    val uiState by LauncherStore.state.collectAsState()
    val presentationState = remember(uiState) {
        PresentationStateController.derive(uiState)
    }
    val context = LocalContext.current
    var layoutSpec by remember { mutableStateOf<LayoutSpecV1?>(null) }
    var themeSpec by remember { mutableStateOf<ThemeSpecV1?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            layoutSpec = ThemeRepository.loadLayoutSpec(context)
            themeSpec = ThemeRepository.loadThemeSpec(context)
        }
    }
    val tokens = remember(themeSpec, uiState.uiDensityMultiplier) {
        ThemeRuntime.fromSpec(themeSpec ?: ThemeSpecV1(), uiState.uiDensityMultiplier)
    }
    var pendingArtworkPick by remember { mutableStateOf<Pair<String, ArtworkType>?>(null) }
    val artworkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingArtworkPick?.let { (gameId, type) ->
            uri?.let { LauncherStore.applyPickedArtwork(context, gameId, type, it) }
        }
        pendingArtworkPick = null
    }
    val onPickArtwork: (String, ArtworkType) -> Unit = { gameId, type ->
        pendingArtworkPick = gameId to type
        artworkPickerLauncher.launch(arrayOf("image/*"))
    }
    MaterialTheme(colorScheme = tokens.toMaterialColorScheme()) {
        CompositionLocalProvider(LocalThemeRuntime provides tokens) {
            val surfaceLayout = layoutSpec?.layoutFor(surfaceRole)
            var isFirstRole by remember { mutableStateOf(true) }
            val swapScale = remember { Animatable(1f) }
            LaunchedEffect(surfaceRole) {
                if (isFirstRole) {
                    isFirstRole = false
                    return@LaunchedEffect
                }
                swapScale.animateTo(1.08f, tween(60))
                swapScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            }
            Box(modifier = Modifier.fillMaxSize().scale(swapScale.value)) {
            if (surfaceLayout != null) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    surfaceLayout.extras.sortedBy { it.order }.forEach { extra ->
                        RenderExtraElement(
                            extra = extra,
                            layoutWidthDp = maxWidth,
                            layoutHeightDp = maxHeight,
                        )
                    }
                    RenderNode(
                        modifier = Modifier.fillMaxSize(),
                        node = surfaceLayout.root,
                        uiState = uiState,
                        presentationState = presentationState,
                        onIntent = onIntent,
                        onPickArtwork = onPickArtwork,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tokens.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Launcher ($surfaceRole)" + if (layoutSpec == null) " — loading…" else " — no layout",
                        color = tokens.onSurface
                    )
                }
            }
            if (surfaceRole == SurfaceRole.INTERACTION && !HomeRoleHelper.isDefaultHome(context) && onSetAsDefaultLauncher != null) {
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                    DefaultLauncherBanner(
                        onClick = onSetAsDefaultLauncher,
                        theme = tokens,
                    )
                }
            }
            if (surfaceRole == SurfaceRole.INTERACTION && uiState.launchError != null) {
                LaunchErrorOverlay(
                    failure = uiState.launchError!!,
                    onDismiss = { onIntent(LauncherIntent.DismissLaunchError) },
                    theme = tokens,
                )
            }
            val hasDualDisplay = DisplayRegistry(context).snapshot().allDisplayIds.size >= 2
            val showSettingsOnThisSurface = uiState.settingsOpen && when {
                !hasDualDisplay -> surfaceRole == SurfaceRole.INTERACTION
                else -> {
                    val preferTop = uiState.settingsDisplayWhenDual == PhysicalSurface.TOP
                    (preferTop && displayId == 0) || (!preferTop && displayId == 4)
                }
            }
            if (showSettingsOnThisSurface) {
                LaunchedEffect(Unit) {
                    com.launcher.aynthords.feature.settings.SettingsStore.focusOnRail()
                }
                SystemSettingsTheme {
                    InShellSettingsOverlay(
                        displayId = displayId,
                        onIntent = onIntent,
                        onReassert = onReassert ?: {},
                        onSetAsDefaultLauncher = onSetAsDefaultLauncher,
                    )
                }
            }
            if (surfaceRole == SurfaceRole.INTERACTION && uiState.detailsEntryId != null) {
                val detailsEntry = uiState.entries.find { it.id == uiState.detailsEntryId }
                if (detailsEntry != null) {
                    DetailsScreen(entry = detailsEntry, onIntent = onIntent, onPickArtwork = onPickArtwork)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onIntent(LauncherIntent.CloseDetails) }
                            .background(tokens.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Entry no longer available. Press any key to close.",
                            color = tokens.onSurfaceVariant,
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun DefaultLauncherBanner(
    onClick: () -> Unit,
    theme: ThemeRuntime,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(theme.spacing)
            .clickable(onClick = onClick)
            .background(theme.surfaceVariant.copy(alpha = 0.98f), RoundedCornerShape(theme.cornerRadius))
            .padding(horizontal = theme.spacing * 2, vertical = theme.spacing),
    ) {
        Text(
            text = "Set as default launcher — HOME will open Ayn Thor Launcher",
            color = theme.onSurfaceVariant,
            fontSize = theme.captionSizeSp,
        )
    }
}

@Composable
private fun LaunchErrorOverlay(
    failure: LaunchFailure,
    onDismiss: () -> Unit,
    theme: ThemeRuntime,
) {
    val message = when (failure) {
        LaunchFailure.MissingIntent -> "This app cannot be launched."
        LaunchFailure.ActivityNotFound -> "App no longer available. It may have been uninstalled."
        LaunchFailure.ResolveFailed -> "Could not start this app."
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.surface.copy(alpha = 0.95f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(theme.spacing * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = theme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Press B to dismiss",
                color = theme.onSurfaceVariant,
                modifier = Modifier.padding(top = theme.spacing),
            )
        }
    }
}

@Composable
fun RenderNode(
    modifier: Modifier,
    node: LayoutNode,
    uiState: LauncherUiState,
    presentationState: PresentationUiState,
    onIntent: (LauncherIntent) -> Unit,
    onPickArtwork: (String, ArtworkType) -> Unit = { _, _ -> },
) {
    if (node.primitive != null && node.children.isEmpty()) {
        RenderPrimitive(node.primitive, modifier, uiState, presentationState, onIntent, onPickArtwork)
        return
    }

    when (node.type) {
        NodeType.Row -> {
            Row(modifier = modifier) {
                node.children.forEach { child ->
                    val childModifier = if (child.weight > 0f) Modifier.weight(child.weight) else Modifier
                    RenderNode(childModifier, child, uiState, presentationState, onIntent, onPickArtwork)
                }
            }
        }
        NodeType.Column -> {
            Column(modifier = modifier) {
                node.children.forEach { child ->
                    val childModifier = if (child.weight > 0f) Modifier.weight(child.weight) else Modifier
                    RenderNode(childModifier, child, uiState, presentationState, onIntent, onPickArtwork)
                }
            }
        }
        NodeType.Stack -> {
            Box(modifier = modifier) {
                node.children.forEach { child ->
                    val alignment = when (child.params["alignment"]) {
                        "CenterEnd" -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                    RenderNode(Modifier.align(alignment), child, uiState, presentationState, onIntent, onPickArtwork)
                }
            }
        }
        else -> {
            // This case should not be reached with the new logic, but left for safety.
        }
    }
}

@Composable
private fun RenderExtraElement(
    extra: ExtraElement,
    layoutWidthDp: Dp,
    layoutHeightDp: Dp,
) {
    val theme = LocalThemeRuntime.current
    val context = LocalContext.current
    val offsetX = (layoutWidthDp.value * extra.posX).dp
    val offsetY = (layoutHeightDp.value * extra.posY).dp
    val sizeW = (layoutWidthDp.value * extra.sizeX).dp
    val sizeH = (layoutHeightDp.value * extra.sizeY).dp
    Box(
        modifier = Modifier
            .offset(offsetX, offsetY)
            .size(sizeW, sizeH)
    ) {
        when (extra.type) {
            ExtraElementType.Image -> {
                if (extra.source.isNotBlank()) {
                    val imageData = if (extra.source.startsWith("http") || extra.source.startsWith("/"))
                        extra.source else "file:///android_asset/${extra.source}"
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(imageData).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            ExtraElementType.Text -> {
                Text(
                    text = extra.source,
                    color = theme.onSurface,
                    modifier = Modifier.padding(theme.spacing)
                )
            }
            ExtraElementType.Shape -> {
                val colorHex = extra.params["color"] ?: "#80000000"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(com.launcher.aynthords.theme.runtime.parseHex(colorHex))
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RenderPrimitive(
    primitive: Primitive,
    modifier: Modifier = Modifier,
    uiState: LauncherUiState,
    presentationState: PresentationUiState,
    onIntent: (LauncherIntent) -> Unit,
    onPickArtwork: (String, ArtworkType) -> Unit = { _, _ -> },
) {
    when (primitive) {
        Primitive.GameGrid -> GameGridPrimitive(modifier, uiState, onIntent)
        Primitive.Rail -> RailPrimitive(modifier, uiState, onIntent)
        Primitive.HintBar -> HintBarPrimitive(modifier, uiState)
        Primitive.BackgroundArt -> BackgroundArtPrimitive(modifier, presentationState)
        Primitive.HeroArt -> HeroArtPrimitive(modifier, presentationState)
        Primitive.MetadataPanel -> MetadataPanelPrimitive(modifier, presentationState)
        Primitive.SelectionIndicator -> SelectionIndicatorPrimitive(modifier, uiState)
        Primitive.DetailsPanel -> {
            val detailsEntry = uiState.entries.find { it.id == uiState.detailsEntryId }
            if (detailsEntry != null) {
                Box(modifier) {
                    DetailsScreen(entry = detailsEntry, onIntent = onIntent, onPickArtwork = onPickArtwork)
                }
            } else {
                Box(modifier = modifier)
            }
        }
    }
}

// --- Primitives ---

@Composable
fun RailPrimitive(
    modifier: Modifier,
    uiState: LauncherUiState,
    onIntent: (LauncherIntent) -> Unit = {},
) {
    val theme = LocalThemeRuntime.current
    Column(
        modifier = modifier
            .width(theme.railWidth)
            .fillMaxHeight()
            .background(theme.surface)
            .padding(theme.spacing),
        verticalArrangement = Arrangement.spacedBy(theme.spacing),
    ) {
        Text(
            text = "Collections",
            color = theme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Box(
            modifier = Modifier
                .background(
                    if (uiState.showFavoritesOnly) theme.primary.copy(alpha = 0.3f)
                    else theme.surfaceVariant,
                    RoundedCornerShape(theme.cornerRadius)
                )
                .clickable { onIntent(LauncherIntent.ToggleFavoritesOnly) }
                .padding(theme.spacing / 2)
        ) {
            Text(
                text = if (uiState.showFavoritesOnly) "Favorites: On" else "Favorites",
                color = theme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun GameGridPrimitive(modifier: Modifier, uiState: LauncherUiState, onIntent: (LauncherIntent) -> Unit) {
    val theme = LocalThemeRuntime.current
    val gridState = rememberLazyGridState()

    if (uiState.entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(theme.surface)
                .padding(theme.spacing),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No apps found. Install apps to see them here.",
                color = theme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LaunchedEffect(uiState.focusedEntryId) {
        val selectedIndex = uiState.entries.indexOfFirst { it.id == uiState.focusedEntryId }
        if (selectedIndex != -1) {
            gridState.animateScrollToItem(selectedIndex)
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(uiState.gridColumns),
        modifier = modifier.fillMaxSize().padding(theme.spacing),
        horizontalArrangement = Arrangement.spacedBy(theme.gridGap),
        verticalArrangement = Arrangement.spacedBy(theme.gridGap)
    ) {
        itemsIndexed(uiState.entries) { index, entry ->
            val isFocused = entry.id == uiState.focusedEntryId
            val borderModifier = if (isFocused) Modifier.border(theme.focusedBorderWidth, theme.primary) else Modifier
            val scaleModifier = if (isFocused) Modifier.scale(theme.focusedScale) else Modifier

            Box(
                modifier = Modifier
                    .aspectRatio(theme.gridItemAspectRatio)
                    .then(scaleModifier)
                    .then(borderModifier)
                    .background(theme.surfaceVariant, RoundedCornerShape(theme.gridItemCornerRadius))
                    .clickable { onIntent(LauncherIntent.SelectEntry(entry.id)) }
            ) {
                AsyncImage(
                    model = entry.heroArtUrl,
                    contentDescription = entry.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun HintBarPrimitive(modifier: Modifier, uiState: LauncherUiState) {
    val theme = LocalThemeRuntime.current
    val config by com.launcher.aynthords.input.InputConfig.state.collectAsState()
    val focusedTitle = uiState.focusedEntry?.label
    val selectedTitle = uiState.selectedEntry?.label

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(theme.hintBarHeight)
            .background(theme.surfaceVariant)
            .padding(theme.spacing / 2),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = theme.spacing),
        ) {
            when {
                uiState.detailsEntryId != null -> {
                    HintGlyph(config, com.launcher.aynthords.input.Action.Back, theme)
                    Text(" Back", color = theme.onSurfaceVariant, fontSize = 12.sp)
                }
                focusedTitle != null && selectedTitle != null && focusedTitle != selectedTitle -> {
                    Text("Focused: $focusedTitle  |  Selected: $selectedTitle  |  ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Confirm, theme)
                    Text(" Confirm & Launch", color = theme.onSurfaceVariant, fontSize = 12.sp)
                }
                selectedTitle != null -> {
                    Text("Selected: $selectedTitle  |  ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Confirm, theme)
                    Text(" Launch ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Back, theme)
                    Text(" Back ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Details, theme)
                    Text(" Details ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Menu, theme)
                    Text(" Options" + if (uiState.launchInProgress) " (launching…)" else "", color = theme.onSurfaceVariant, fontSize = 12.sp)
                }
                focusedTitle != null -> {
                    Text("Focused: $focusedTitle  |  ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Confirm, theme)
                    Text(" Confirm & Launch ", color = theme.onSurfaceVariant, fontSize = 12.sp)
                    HintGlyph(config, com.launcher.aynthords.input.Action.Details, theme)
                    Text(" Details", color = theme.onSurfaceVariant, fontSize = 12.sp)
                }
                else -> Text("Use DPAD to focus an app", color = theme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HintGlyph(
    config: com.launcher.aynthords.input.InputConfigState,
    action: com.launcher.aynthords.input.Action,
    theme: com.launcher.aynthords.theme.runtime.ThemeRuntime,
) {
    com.launcher.aynthords.input.ActionGlyph(
        action = action,
        config = config,
        size = 20.dp,
        backgroundColor = theme.surfaceVariant.copy(alpha = 0.5f),
        textColor = theme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BackgroundArtPrimitive(modifier: Modifier, state: PresentationUiState) {
    val theme = LocalThemeRuntime.current
    AnimatedContent(
        targetState = state.backgroundArtUrl,
        transitionSpec = {
            fadeIn(animationSpec = tween(theme.motionDurationMs)) with
                fadeOut(animationSpec = tween(theme.motionDurationMs))
        }
    ) { url ->
        when (state.loadingState) {
            PresentationLoadingState.LOADING -> {
                Box(modifier = modifier.fillMaxSize().background(theme.surfaceVariant))
            }
            PresentationLoadingState.ERROR -> {
                Box(modifier = modifier.fillMaxSize().background(theme.surface)) {
                    Text(
                        text = "Unable to load artwork",
                        color = theme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.BottomStart).padding(theme.spacing)
                    )
                }
            }
            PresentationLoadingState.EMPTY,
            PresentationLoadingState.IDLE -> {
                if (url != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Game Background",
                        contentScale = ContentScale.Crop,
                        modifier = modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = modifier.fillMaxSize().background(theme.surface))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HeroArtPrimitive(modifier: Modifier, state: PresentationUiState) {
    val theme = LocalThemeRuntime.current
    AnimatedContent(
        targetState = state.heroArtUrl,
        transitionSpec = {
            fadeIn(animationSpec = tween(theme.motionDurationMs)) with
                fadeOut(animationSpec = tween(theme.motionDurationMs))
        }
    ) { url ->
        when (state.loadingState) {
            PresentationLoadingState.LOADING -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading…",
                        color = theme.onSurfaceVariant
                    )
                }
            }
            PresentationLoadingState.ERROR -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading game",
                        color = theme.onSurfaceVariant
                    )
                }
            }
            PresentationLoadingState.EMPTY,
            PresentationLoadingState.IDLE -> {
                if (url != null) {
                    Box(
                        modifier = modifier.fillMaxSize().padding(theme.spacing),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Game Hero Art"
                        )
                    }
                } else {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a game",
                            color = theme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MetadataPanelPrimitive(modifier: Modifier, state: PresentationUiState) {
    val theme = LocalThemeRuntime.current
    AnimatedContent(
        targetState = state.title,
        transitionSpec = {
            fadeIn(animationSpec = tween(theme.motionDurationMs)) with
                fadeOut(animationSpec = tween(theme.motionDurationMs))
        }
    ) {
        when (state.loadingState) {
            PresentationLoadingState.LOADING -> {
                Box(
                    modifier = modifier
                        .fillMaxSize(theme.metadataPanelSizeFraction)
                        .background(theme.surface.copy(alpha = 0.9f))
                        .padding(theme.spacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading details…",
                        color = theme.onSurfaceVariant,
                        fontSize = theme.bodySizeSp
                    )
                }
            }
            PresentationLoadingState.ERROR -> {
                Box(
                    modifier = modifier
                        .fillMaxSize(theme.metadataPanelSizeFraction)
                        .background(theme.surface.copy(alpha = 0.9f))
                        .padding(theme.spacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading details",
                        color = theme.onSurfaceVariant,
                        fontSize = theme.bodySizeSp
                    )
                }
            }
            PresentationLoadingState.EMPTY,
            PresentationLoadingState.IDLE -> {
                if (state.title != null) {
                    Column(
                        modifier = modifier
                            .fillMaxSize(theme.metadataPanelSizeFraction)
                            .background(theme.surface.copy(alpha = 0.9f))
                            .padding(theme.spacing),
                    ) {
                        Text(
                            text = state.title,
                            color = theme.onSurface,
                            fontSize = theme.titleSizeSp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.subtitle != null) {
                            Text(
                                text = state.subtitle,
                                color = theme.onSurfaceVariant,
                                fontSize = theme.bodySizeSp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = modifier
                            .fillMaxSize(theme.metadataPanelSizeFraction)
                            .padding(theme.spacing),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a game",
                            color = theme.onSurfaceVariant,
                            fontSize = theme.bodySizeSp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionIndicatorPrimitive(modifier: Modifier, uiState: LauncherUiState) {
    val theme = LocalThemeRuntime.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(theme.spacing / 4),
        contentAlignment = Alignment.Center
    ) {
        val drivingEntry = uiState.focusedEntry ?: uiState.selectedEntry
        val displayTitle = drivingEntry?.label
        if (displayTitle != null) {
            Box(
                modifier = Modifier
                    .padding(theme.spacing / 8)
                    .background(theme.primary.copy(alpha = 0.6f), shape = RoundedCornerShape(theme.cornerRadius))
                    .padding(horizontal = theme.spacing / 2, vertical = theme.spacing / 4)
            ) {
                Text("Selected: $displayTitle", color = theme.surface)
            }
        }
    }
}
