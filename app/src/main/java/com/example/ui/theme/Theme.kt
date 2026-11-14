package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RockySlotColorScheme =
  darkColorScheme(
    primary = ChampionshipGoldRich,
    onPrimary = ArenaObsidian,
    primaryContainer = ChampionshipBronze,
    onPrimaryContainer = ChampionshipGold,
    secondary = RockyCrimson,
    onSecondary = CreamWhite,
    secondaryContainer = RockyCrimsonDark,
    onSecondaryContainer = CreamWhite,
    tertiary = RingsideBlueBright,
    onTertiary = CreamWhite,
    background = ArenaObsidian,
    onBackground = CreamWhite,
    surface = ArenaDeepSlate,
    onSurface = CreamWhite,
    surfaceVariant = CabinetMetalDark,
    onSurfaceVariant = MutedSilver,
    outline = CabinetBevel,
  )

@Composable
fun RockySlotTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = RockySlotColorScheme, typography = Typography, content = content)
}
