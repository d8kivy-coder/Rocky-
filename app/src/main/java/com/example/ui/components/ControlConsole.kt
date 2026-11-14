package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.slot.engine.formatCredits
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.BebasNeueFamily
import com.example.ui.theme.CabinetBevel
import com.example.ui.theme.CabinetMetalDark
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ChampionshipGoldDeep
import com.example.ui.theme.ChampionshipGoldRich
import com.example.ui.theme.CreamWhite
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.OswaldFamily
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RockyCrimson
import com.example.ui.theme.RockyCrimsonBright
import com.example.ui.theme.WinEmerald
import com.example.ui.theme.WinEmeraldDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaytechActionRail(
  buyBonusCostCents: Long,
  isSpinning: Boolean,
  inFreeSpinsMode: Boolean,
  freeSpinsRemaining: Int,
  isAutoPlayActive: Boolean,
  autoPlayRemaining: Int,
  turboEnabled: Boolean,
  canGamble: Boolean,
  pendingWinCents: Long,
  horizontalMode: Boolean,
  onBuyBonus: () -> Unit,
  onSpinOrStop: () -> Unit,
  onAutoPlay: () -> Unit,
  onOpenBetSelector: () -> Unit,
  onToggleTurbo: () -> Unit,
  onOpenGamble: () -> Unit,
  onCollectWin: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val controlsLocked = isSpinning || inFreeSpinsMode

  val content: @Composable () -> Unit = {
    // BUY BONUS oval pill matching Playtech screenshot
    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(50))
          .background(Brush.verticalGradient(listOf(Color(0xFF282E36), Color(0xFF10141A))))
          .border(1.5.dp, ChampionshipGoldRich, RoundedCornerShape(50))
          .clickable(enabled = !controlsLocked, onClick = onBuyBonus)
          .padding(horizontal = 14.dp, vertical = 7.dp)
          .testTag("buy_bonus_button"),
      contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "BUY",
          color = if (!controlsLocked) ChampionshipGold else MutedSilver,
          fontFamily = OswaldFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          lineHeight = 11.sp,
        )
        Text(
          text = "BONUS",
          color = if (!controlsLocked) ChampionshipGold else MutedSilver,
          fontFamily = OswaldFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          lineHeight = 11.sp,
        )
      }
    }

    // Iconic Playtech Circular Gold Spin Arrow Button ("HOLD SPIN FOR AUTO PLAY")
    Box(
      modifier =
        Modifier.size(84.dp)
          .clip(CircleShape)
          .background(Color(0xD910161E))
          .border(
            width = 2.dp,
            brush =
              Brush.linearGradient(
                if (isSpinning) listOf(RockyCrimsonBright, RockyCrimson)
                else listOf(ChampionshipGold, ChampionshipGoldDeep)
              ),
            shape = CircleShape,
          )
          .combinedClickable(
            onClick = {
              if (isAutoPlayActive) onAutoPlay() else onSpinOrStop()
            },
            onLongClick = {
              if (!isSpinning && !inFreeSpinsMode) onAutoPlay()
            },
          )
          .testTag("spin_button"),
      contentAlignment = Alignment.Center,
    ) {
      if (isSpinning) {
        Icon(
          imageVector = Icons.Filled.Stop,
          contentDescription = stringResource(R.string.btn_stop),
          tint = RockyCrimsonBright,
          modifier = Modifier.size(34.dp),
        )
      } else if (isAutoPlayActive) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Filled.Stop,
            contentDescription = stringResource(R.string.btn_stop_auto),
            tint = ChampionshipGold,
            modifier = Modifier.size(22.dp),
          )
          Text(
            text = "${autoPlayRemaining + 1}",
            color = ChampionshipGold,
            fontFamily = RobotoMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
          )
        }
      } else if (inFreeSpinsMode) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "FREE",
            color = ChampionshipGold,
            fontFamily = BebasNeueFamily,
            fontSize = 18.sp,
            lineHeight = 18.sp,
          )
          Text(
            text = freeSpinsRemaining.toString(),
            color = CreamWhite,
            fontFamily = RobotoMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
          )
        }
      } else {
        // Sweeping Gold Circular Spin Arrow Canvas
        Canvas(modifier = Modifier.size(52.dp)) {
          val strokeW = size.width * 0.14f
          drawArc(
            brush = Brush.sweepGradient(listOf(ChampionshipGoldDeep, ChampionshipGold, ChampionshipGoldRich)),
            startAngle = -55f,
            sweepAngle = 275f,
            useCenter = false,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
          )
          // Arrowhead at top-right
          val tipX = size.width * 0.80f
          val tipY = size.height * 0.24f
          drawLine(
            color = ChampionshipGold,
            start = Offset(tipX - 10f, tipY - 8f),
            end = Offset(tipX + 3f, tipY + 5f),
            strokeWidth = strokeW * 0.8f,
            cap = StrokeCap.Round,
          )
          drawLine(
            color = ChampionshipGold,
            start = Offset(tipX + 6f, tipY - 9f),
            end = Offset(tipX + 3f, tipY + 5f),
            strokeWidth = strokeW * 0.8f,
            cap = StrokeCap.Round,
          )
        }
        Text(
          text = "HOLD AUTO",
          color = CreamWhite.copy(alpha = 0.72f),
          fontFamily = OswaldFamily,
          fontSize = 7.5.sp,
          letterSpacing = 0.5.sp,
          modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp),
        )
      }
    }

    // Bet Selector Coins Pill matching screenshot
    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(50))
          .background(Brush.verticalGradient(listOf(Color(0xFF282E36), Color(0xFF10141A))))
          .border(1.5.dp, ChampionshipGoldRich, RoundedCornerShape(50))
          .clickable(enabled = !controlsLocked, onClick = onOpenBetSelector)
          .padding(horizontal = 14.dp, vertical = 9.dp)
          .testTag("bet_selector_button"),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.MonetizationOn,
          contentDescription = "Select Bet",
          tint = ChampionshipGold,
          modifier = Modifier.size(18.dp),
        )
        Icon(
          imageVector = Icons.Filled.Tune,
          contentDescription = null,
          tint = CreamWhite,
          modifier = Modifier.size(16.dp),
        )
      }
    }

    // Compact Turbo + Gamble/Collect utility pills
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(if (turboEnabled) Color(0xFF3E2D00) else Color(0xCC121820))
            .border(1.dp, if (turboEnabled) ChampionshipGold else CabinetBevel, CircleShape)
            .clickable(onClick = onToggleTurbo)
            .testTag("turbo_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Bolt,
          contentDescription = stringResource(R.string.btn_turbo),
          tint = if (turboEnabled) ChampionshipGold else MutedSilver,
          modifier = Modifier.size(20.dp),
        )
      }

      if (canGamble) {
        Box(
          modifier =
            Modifier.height(44.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(RockyCrimson)
              .border(1.5.dp, ChampionshipGold, RoundedCornerShape(22.dp))
              .clickable(onClick = onOpenGamble)
              .padding(horizontal = 10.dp)
              .testTag("gamble_button"),
          contentAlignment = Alignment.Center,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Filled.Casino, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(16.dp))
            Text("GAMBLE", color = ChampionshipGold, fontFamily = BebasNeueFamily, fontSize = 15.sp)
          }
        }
      } else if (pendingWinCents > 0L && !isSpinning) {
        Box(
          modifier =
            Modifier.height(44.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(WinEmeraldDark)
              .border(1.5.dp, WinEmerald, RoundedCornerShape(22.dp))
              .clickable(onClick = onCollectWin)
              .padding(horizontal = 10.dp)
              .testTag("collect_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text("COLLECT", color = CreamWhite, fontFamily = BebasNeueFamily, fontSize = 15.sp)
        }
      }
    }
  }

  if (horizontalMode) {
    Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      content()
    }
  } else {
    Column(
      modifier = modifier.padding(horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      content()
    }
  }
}

@Composable
fun PlaytechBottomFooter(
  balanceCents: Long,
  coinValueCents: Long,
  totalBetCents: Long,
  displayWinCents: Long,
  statusMessage: String,
  onTopUp: () -> Unit,
  onOpenBetSelector: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xE6080B10))
        .border(1.dp, CabinetBevel.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
        .padding(horizontal = 12.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    // Left: BALANCE (tap to top up free play chips)
    Column(
      modifier =
        Modifier.clickable(onClick = onTopUp)
          .padding(end = 6.dp)
          .testTag("balance_topup_button"),
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = formatCredits(balanceCents),
        color = CreamWhite,
        fontFamily = RobotoMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
      )
      Text(
        text = "BALANCE  (+TOP UP)",
        color = MutedSilver,
        fontFamily = OswaldFamily,
        fontSize = 9.sp,
        letterSpacing = 0.6.sp,
      )
    }

    // Center: Status / Win / Coin Value
    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val hasWin = displayWinCents > 0L
      Text(
        text = if (hasWin) "WIN ${formatCredits(displayWinCents)}  •  $statusMessage" else statusMessage,
        color = if (hasWin) WinEmerald else CreamWhite,
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
      Text(
        text = "COIN VALUE = ${formatCredits(coinValueCents)}  •  40 LINES",
        color = MutedSilver,
        fontFamily = RobotoMonoFamily,
        fontSize = 9.5.sp,
        maxLines = 1,
      )
    }

    // Right: TOTAL BET
    Column(
      modifier = Modifier.clickable(onClick = onOpenBetSelector).padding(start = 6.dp),
      horizontalAlignment = Alignment.End,
    ) {
      Text(
        text = formatCredits(totalBetCents),
        color = ChampionshipGold,
        fontFamily = RobotoMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
      )
      Text(
        text = stringResource(R.string.label_total_bet),
        color = MutedSilver,
        fontFamily = OswaldFamily,
        fontSize = 9.sp,
        letterSpacing = 0.6.sp,
      )
    }
  }
}
