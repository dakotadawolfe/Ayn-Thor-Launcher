package com.launcher.aynthords.feature.details

import com.launcher.aynthords.data.local.ArtworkType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.launcher.aynthords.domain.model.ResolvedGameModel
import com.launcher.aynthords.feature.details.DetailsContract
import com.launcher.aynthords.feature.details.toDetailsContract
import com.launcher.aynthords.domain.model.SurfaceRole
import com.launcher.aynthords.feature.launcher.LauncherIntent
import com.launcher.aynthords.theme.runtime.LocalThemeRuntime

@Composable
fun DetailsScreen(
    entry: ResolvedGameModel,
    onIntent: (LauncherIntent) -> Unit,
    onPickArtwork: (gameId: String, type: ArtworkType) -> Unit = { _, _ -> },
) {
    val theme = LocalThemeRuntime.current
    val contract = entry.toDetailsContract()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(theme.spacing * 2),
            verticalArrangement = Arrangement.spacedBy(theme.spacing),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(theme.surfaceVariant, RoundedCornerShape(theme.cornerRadius))
                    .padding(theme.spacing),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = contract.heroArtUrl,
                    contentDescription = contract.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                text = contract.title,
                color = theme.onSurface,
                fontSize = theme.titleSizeSp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = theme.spacing / 2),
            )
            if (contract.notes.isNotBlank()) {
                Text(
                    text = contract.notes,
                    color = theme.onSurfaceVariant,
                    fontSize = theme.bodySizeSp,
                    modifier = Modifier.padding(horizontal = theme.spacing / 2),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = theme.spacing),
                horizontalArrangement = Arrangement.spacedBy(theme.spacing),
            ) {
                ActionChip(
                    label = "Launch",
                    onClick = {
                        onIntent(LauncherIntent.SelectEntry(entry.id))
                        onIntent(LauncherIntent.ConfirmSelection)
                        onIntent(LauncherIntent.LaunchSelected)
                        onIntent(LauncherIntent.CloseDetails)
                    },
                    theme = theme,
                )
                ActionChip(
                    label = if (entry.isFavorite) "Unfavorite" else "Favorite",
                    onClick = { onIntent(LauncherIntent.ToggleFavorite(entry.id)) },
                    theme = theme,
                )
                ActionChip(
                    label = if (entry.isHidden) "Unhide" else "Hide",
                    onClick = { onIntent(LauncherIntent.ToggleHidden(entry.id)) },
                    theme = theme,
                )
                ActionChip(
                    label = "Role: ${(entry.launchPolicyOverride?.preferredLogicalRole ?: SurfaceRole.INTERACTION).name}",
                    onClick = {
                        val next = if (entry.launchPolicyOverride?.preferredLogicalRole == SurfaceRole.PRESENTATION)
                            SurfaceRole.INTERACTION else SurfaceRole.PRESENTATION
                        onIntent(LauncherIntent.SetLaunchRolePreference(entry.id, next))
                    },
                    theme = theme,
                )
                ActionChip(
                    label = "Change Hero Art",
                    onClick = { onPickArtwork(entry.id, ArtworkType.Hero) },
                    theme = theme,
                )
                ActionChip(
                    label = "Back",
                    onClick = { onIntent(LauncherIntent.CloseDetails) },
                    theme = theme,
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    onClick: () -> Unit,
    theme: com.launcher.aynthords.theme.runtime.ThemeRuntime
) {
    Box(
        modifier = Modifier
            .background(theme.surfaceVariant, RoundedCornerShape(theme.cornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = theme.spacing, vertical = theme.spacing / 2),
    ) {
        Text(
            text = label,
            color = theme.onSurfaceVariant,
            fontSize = theme.bodySizeSp,
        )
    }
}
