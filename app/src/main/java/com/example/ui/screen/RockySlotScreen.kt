package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.slot.engine.RockySlotEngine
import com.example.slot.engine.formatCredits
import com.example.slot.viewmodel.RockySlotUiState
import com.example.slot.viewmodel.RockySlotViewModel
import com.example.ui.components.GambleScreenOverlay
import com.example.ui.components.KnockoutBonusDialog
import com.example.ui.components.MovieBonusHubDialog
import com.example.ui.components.PaytableModal
import com.example.ui.components.PhiladelphiaStepsBackdrop
import com.example.ui.components.PlaytechActionRail
import com.example.ui.components.PlaytechBottomFooter
import com.example.ui.components.ReelCabinet
import com.example.ui.components.WinCelebrationOverlay
import com.example.ui.theme.ArenaDeepSlate
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.BebasNeueFamily
import com.example.ui.theme.CabinetBevel
import com.example.ui.theme.CabinetMetalDark
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ChampionshipGoldRich
import com.example.ui.theme.CreamWhite
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.OswaldFamily
import com.example.ui.theme.RingsideBlue
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RockyCrimson

@Composable
fun RockySlotScreen(viewModel: RockySlotViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val haptics = LocalHapticFeedback.current

  LaunchedEffect(state.lastEvaluation) {
    val eval = state.lastEvaluation ?: return@LaunchedEffect
    if (eval.bonusTriggered || eval.totalWinCents >= state.totalBetCents * 5L) {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Misty Philadelphia Art Museum Steps + Rocky Arms-Raised Silhouette Backdrop
    PhiladelphiaStepsBackdrop()

    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // Top Playtech Header: (i) Info + ROCKY Logo + Free Games / Sound Toggle
      TopRockyHeader(
        state = state,
        onOpenInfo = { viewModel.setPaytableVisible(true) },
        onToggleSound = viewModel::toggleSound,
      )

      // Active Free Games Banner when in Rocky I / III Free Spins
      AnimatedVisibility(
        visible = state.inFreeSpinsMode,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(RockyCrimson.copy(alpha = 0.88f))
              .border(1.5.dp, ChampionshipGold, RoundedCornerShape(6.dp))
              .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${state.activeFeatureLabel ?: "FREE GAMES"} • ${state.freeSpinsRemaining} LEFT",
              color = ChampionshipGold,
              fontFamily = BebasNeueFamily,
              fontSize = 18.sp,
            )
            Text(
              text = "FG WIN: ${formatCredits(state.freeSpinsTotalWinCents)}",
              color = CreamWhite,
              fontFamily = RobotoMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
            )
          }
        }
      }

      // Responsive Center Stage: 5x5 Reel Cabinet + Right/Bottom Action Rail
      BoxWithConstraints(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            // Left breathing space showing Rocky silhouette on the steps
            Spacer(modifier = Modifier.weight(0.32f))

            Box(
              modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 4.dp),
              contentAlignment = Alignment.Center,
            ) {
              ReelCabinet(
                grid = state.grid,
                reelSpinning = state.reelSpinning,
                inFreeSpinsMode = state.inFreeSpinsMode,
                stickyWildPositions = state.stickyWildPositions,
                lastEvaluation = state.lastEvaluation,
                highlightedLineIndex = state.highlightedLineIndex,
              )
            }

            Box(
              modifier = Modifier.weight(0.38f).fillMaxHeight(),
              contentAlignment = Alignment.Center,
            ) {
              PlaytechActionRail(
                buyBonusCostCents = state.buyBonusCostCents,
                isSpinning = state.isSpinning,
                inFreeSpinsMode = state.inFreeSpinsMode,
                freeSpinsRemaining = state.freeSpinsRemaining,
                isAutoPlayActive = state.isAutoPlayActive,
                autoPlayRemaining = state.autoPlayRemaining,
                turboEnabled = state.turboEnabled,
                canGamble = state.canGamble,
                pendingWinCents = state.pendingWinCents,
                horizontalMode = false,
                onBuyBonus = viewModel::buyBonus,
                onSpinOrStop = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  viewModel.onSpinOrStopClicked()
                },
                onAutoPlay = viewModel::openAutoPlayDialog,
                onOpenBetSelector = { viewModel.openBetSelector(true) },
                onToggleTurbo = viewModel::toggleTurbo,
                onOpenGamble = viewModel::openGamble,
                onCollectWin = viewModel::collectWin,
              )
            }
          }
        } else {
          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Box(
              modifier = Modifier.weight(1f).fillMaxWidth(),
              contentAlignment = Alignment.Center,
            ) {
              ReelCabinet(
                grid = state.grid,
                reelSpinning = state.reelSpinning,
                inFreeSpinsMode = state.inFreeSpinsMode,
                stickyWildPositions = state.stickyWildPositions,
                lastEvaluation = state.lastEvaluation,
                highlightedLineIndex = state.highlightedLineIndex,
                modifier = Modifier.widthIn(max = 440.dp),
              )
            }

            PlaytechActionRail(
              buyBonusCostCents = state.buyBonusCostCents,
              isSpinning = state.isSpinning,
              inFreeSpinsMode = state.inFreeSpinsMode,
              freeSpinsRemaining = state.freeSpinsRemaining,
              isAutoPlayActive = state.isAutoPlayActive,
              autoPlayRemaining = state.autoPlayRemaining,
              turboEnabled = state.turboEnabled,
              canGamble = state.canGamble,
              pendingWinCents = state.pendingWinCents,
              horizontalMode = true,
              onBuyBonus = viewModel::buyBonus,
              onSpinOrStop = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onSpinOrStopClicked()
              },
              onAutoPlay = viewModel::openAutoPlayDialog,
              onOpenBetSelector = { viewModel.openBetSelector(true) },
              onToggleTurbo = viewModel::toggleTurbo,
              onOpenGamble = viewModel::openGamble,
              onCollectWin = viewModel::collectWin,
            )
          }
        }
      }

      // Bottom Dark Status Footer matching Playtech Screenshot
      val displayWin =
        if (state.pendingWinCents > 0L) state.pendingWinCents else state.lastPaidWinCents
      PlaytechBottomFooter(
        balanceCents = state.balanceCents,
        coinValueCents = state.coinValueCents,
        totalBetCents = state.totalBetCents,
        displayWinCents = displayWin,
        statusMessage = state.statusMessage,
        onTopUp = viewModel::topUpFreeCredits,
        onOpenBetSelector = { viewModel.openBetSelector(true) },
      )
    }

    // Modals & Feature Overlays
    if (state.showBetSelector) {
      BetSelectorDialog(
        currentBetCents = state.totalBetCents,
        onSelectBet = viewModel::selectTotalBet,
        onDismiss = { viewModel.openBetSelector(false) },
      )
    }

    if (state.showAutoPlayDialog) {
      AutoPlaySelectorDialog(
        onSelectCount = viewModel::startAutoPlay,
        onDismiss = viewModel::dismissAutoPlayDialog,
      )
    }

    if (state.showPaytable) {
      PaytableModal(
        stats = state.stats,
        onDismiss = { viewModel.setPaytableVisible(false) },
      )
    }

    state.movieBonusHub?.let { hubState ->
      MovieBonusHubDialog(
        state = hubState,
        onSelectFeature = viewModel::selectMovieFeature,
        onPickGlove = viewModel::pickBonusGlove,
        onSpinWheel = viewModel::spinChampionshipWheel,
        onAdvanceTraining = viewModel::advanceTrainingSteps,
        onCollect = viewModel::collectMovieBonusHub,
      )
    }

    state.knockoutBonus?.let { bonusState ->
      KnockoutBonusDialog(
        state = bonusState,
        onSelectOpponent = viewModel::selectKnockoutOpponent,
        onFightRound = viewModel::fightNextKnockoutRound,
        onToggleAutoFight = viewModel::toggleAutoFightKnockout,
        onCollect = viewModel::collectKnockoutBonus,
      )
    }

    state.gambleState?.let { gamble ->
      GambleScreenOverlay(
        state = gamble,
        onPickColor = viewModel::gamblePickColor,
        onGambleHalf = viewModel::gambleHalf,
        onCollect = viewModel::collectGamble,
      )
    }

    state.winCelebration?.let { celebration ->
      WinCelebrationOverlay(
        celebration = celebration,
        onDismiss = viewModel::dismissWinCelebration,
      )
    }
  }
}

@Composable
private fun TopRockyHeader(
  state: RockySlotUiState,
  onOpenInfo: () -> Unit,
  onToggleSound: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Iconic Playtech Gold-Outlined Circular (i) Info Button
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(Color(0xCC121922))
            .border(1.5.dp, ChampionshipGoldRich, CircleShape)
            .clickable(onClick = onOpenInfo)
            .testTag("info_button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "i",
          color = ChampionshipGold,
          fontFamily = OswaldFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
        )
      }

      // Bold Crisp White ROCKY Block Logo + Americana Red-White-Blue Ribbon
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "★ ROCKY ★",
            color = CreamWhite,
            fontFamily = BebasNeueFamily,
            fontSize = 34.sp,
            letterSpacing = 1.5.sp,
            lineHeight = 32.sp,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier =
              Modifier.clip(RoundedCornerShape(4.dp))
                .background(RingsideBlue)
                .border(1.dp, ChampionshipGold, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = stringResource(R.string.play_for_free_badge),
              color = CreamWhite,
              fontFamily = OswaldFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
            )
          }
        }
        Row(
          modifier = Modifier.width(160.dp).heightIn(min = 3.dp).clip(RoundedCornerShape(2.dp)),
        ) {
          Box(modifier = Modifier.weight(1f).heightIn(min = 3.dp).background(RockyCrimson))
          Box(modifier = Modifier.weight(1f).heightIn(min = 3.dp).background(CreamWhite))
          Box(modifier = Modifier.weight(1f).heightIn(min = 3.dp).background(RingsideBlue))
        }
      }
    }

    IconButton(
      onClick = onToggleSound,
      modifier =
        Modifier.size(48.dp)
          .clip(CircleShape)
          .background(Color(0xAA101720))
          .border(1.dp, CabinetBevel, CircleShape)
          .testTag("sound_toggle_button"),
    ) {
      Icon(
        imageVector =
          if (state.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp
          else Icons.AutoMirrored.Filled.VolumeOff,
        contentDescription = stringResource(R.string.cd_sound_toggle),
        tint = if (state.soundEnabled) ChampionshipGold else MutedSilver,
      )
    }
  }
}

@Composable
private fun BetSelectorDialog(
  currentBetCents: Long,
  onSelectBet: (Long) -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier.widthIn(max = 360.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(ArenaDeepSlate)
          .border(2.dp, ChampionshipGoldRich, RoundedCornerShape(14.dp))
          .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "SELECT TOTAL BET (40 LINES)",
        color = ChampionshipGold,
        fontFamily = BebasNeueFamily,
        fontSize = 24.sp,
        letterSpacing = 1.sp,
      )
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(RockySlotEngine.TOTAL_BET_OPTIONS_CENTS) { bet ->
          val selected = bet == currentBetCents
          Box(
            modifier =
              Modifier.heightIn(min = 50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) RockyCrimson else CabinetMetalDark)
                .border(
                  width = if (selected) 2.dp else 1.dp,
                  color = if (selected) ChampionshipGold else CabinetBevel,
                  shape = RoundedCornerShape(8.dp),
                )
                .clickable { onSelectBet(bet) }
                .padding(6.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = formatCredits(bet),
              color = if (selected) ChampionshipGold else CreamWhite,
              fontFamily = RobotoMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
            )
          }
        }
      }
      TextButton(onClick = onDismiss) {
        Text(text = "CLOSE", color = MutedSilver, fontFamily = OswaldFamily)
      }
    }
  }
}

@Composable
private fun AutoPlaySelectorDialog(
  onSelectCount: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier.widthIn(max = 340.dp)
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(ArenaDeepSlate)
          .border(2.dp, ChampionshipGoldRich, RoundedCornerShape(14.dp))
          .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "SELECT AUTO PLAY SPINS",
        color = ChampionshipGold,
        fontFamily = BebasNeueFamily,
        fontSize = 24.sp,
        letterSpacing = 1.5.sp,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf(10, 25, 50, 99).forEach { count ->
          Button(
            onClick = { onSelectCount(count) },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("autoplay_${count}_button"),
            colors = ButtonDefaults.buttonColors(containerColor = RingsideBlue),
            shape = RoundedCornerShape(8.dp),
          ) {
            Text(
              text = count.toString(),
              fontFamily = BebasNeueFamily,
              fontSize = 20.sp,
              color = CreamWhite,
            )
          }
        }
      }
      TextButton(onClick = onDismiss) {
        Text(text = "CANCEL", color = MutedSilver, fontFamily = OswaldFamily)
      }
    }
  }
}
