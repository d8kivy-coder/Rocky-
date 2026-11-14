package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.slot.engine.formatCredits
import com.example.slot.model.CardColor
import com.example.slot.model.GambleCard
import com.example.slot.model.GambleState
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
import com.example.ui.theme.RingsideBlueDark
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RockyCrimson
import com.example.ui.theme.RockyCrimsonBright
import com.example.ui.theme.WinEmerald
import com.example.ui.theme.WinEmeraldDark

@Composable
fun GambleScreenOverlay(
  state: GambleState,
  onPickColor: (CardColor) -> Unit,
  onGambleHalf: () -> Unit,
  onCollect: () -> Unit,
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(ArenaObsidian.copy(alpha = 0.94f))
          .padding(16.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 460.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
              Brush.verticalGradient(listOf(Color(0xFF1A2236), ArenaDeepSlate))
            )
            .border(2.5.dp, ChampionshipGoldRich, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = stringResource(R.string.gamble_title),
          color = ChampionshipGold,
          fontFamily = BebasNeueFamily,
          fontSize = 32.sp,
          letterSpacing = 2.sp,
        )

        // Meters: Current Stake | Double To | Banked
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          GambleMeter(
            label = "STAKE",
            value = formatCredits(state.currentWinCents),
            color = CreamWhite,
            modifier = Modifier.weight(1f),
          )
          GambleMeter(
            label = "DOUBLE TO",
            value = formatCredits(state.currentWinCents * 2L),
            color = ChampionshipGold,
            modifier = Modifier.weight(1f),
          )
          if (state.bankedCents > 0L) {
            GambleMeter(
              label = "BANKED",
              value = formatCredits(state.bankedCents),
              color = WinEmerald,
              modifier = Modifier.weight(1f),
            )
          }
        }

        // Previous Cards History Strip
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "PREVIOUS CARDS",
            color = MutedSilver,
            fontFamily = OswaldFamily,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.history.forEach { card -> MiniCardBadge(card) }
          }
        }

        // Main Animated Playing Card
        val flipAngle by
          animateFloatAsState(
            targetValue = if (state.isFlipping) 180f else 0f,
            animationSpec = tween(durationMillis = 360),
            label = "card_flip",
          )

        Box(
          modifier =
            Modifier.size(width = 116.dp, height = 160.dp)
              .graphicsLayer {
                rotationY = flipAngle
                cameraDistance = 12f * density
              }
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (state.isFlipping || state.lastDrawnCard == null) {
                  Brush.verticalGradient(listOf(RingsideBlueDark, RockyCrimson))
                } else {
                  Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1)))
                }
              )
              .border(2.dp, ChampionshipGold, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center,
        ) {
          val card = state.lastDrawnCard
          if (state.isFlipping || card == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "★",
                color = ChampionshipGold,
                fontSize = 34.sp,
              )
              Text(
                text = "ROCKY",
                color = CreamWhite,
                fontFamily = BebasNeueFamily,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
              )
            }
          } else {
            val suitColor = if (card.suit.isRed) Color(0xFFD32F2F) else Color(0xFF14171F)
            Column(
              modifier = Modifier.fillMaxSize().padding(10.dp),
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = "${card.rank}${card.suit.symbol}",
                color = suitColor,
                fontFamily = OswaldFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start),
              )
              Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 48.sp,
              )
              Text(
                text = "${card.rank}${card.suit.symbol}",
                color = suitColor,
                fontFamily = OswaldFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.End),
              )
            }
          }
        }

        // Result feedback
        val statusText =
          when {
            state.isFlipping -> "DRAWING CARD..."
            state.lastGuessWon == true -> "DOUBLE WIN! PICK AGAIN OR COLLECT!"
            state.lastGuessWon == false ->
              if (state.bankedCents > 0L) "CARD MISSED — COLLECT YOUR BANKED ${formatCredits(state.bankedCents)}!"
              else "WRONG COLOR — BETTER LUCK NEXT SPIN!"
            else -> "PREDICT RED OR BLACK TO DOUBLE YOUR WIN"
          }
        Text(
          text = statusText,
          color =
            when (state.lastGuessWon) {
              true -> WinEmerald
              false -> RockyCrimsonBright
              null -> CreamWhite
            },
          fontFamily = OswaldFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          textAlign = TextAlign.Center,
        )

        // RED / BLACK choice buttons
        if (!state.isFinished) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = { onPickColor(CardColor.RED) },
              enabled = !state.isFlipping,
              modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("gamble_red_button"),
              colors = ButtonDefaults.buttonColors(containerColor = RockyCrimson),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = "♥ ♦ ${stringResource(R.string.gamble_red)}",
                fontFamily = BebasNeueFamily,
                fontSize = 22.sp,
                color = CreamWhite,
              )
            }
            Button(
              onClick = { onPickColor(CardColor.BLACK) },
              enabled = !state.isFlipping,
              modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("gamble_black_button"),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = "♠ ♣ ${stringResource(R.string.gamble_black)}",
                fontFamily = BebasNeueFamily,
                fontSize = 22.sp,
                color = CreamWhite,
              )
            }
          }
        }

        // GAMBLE HALF + COLLECT row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (!state.isFinished && state.currentWinCents >= 2L) {
            OutlinedButton(
              onClick = onGambleHalf,
              enabled = !state.isFlipping,
              modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("gamble_half_button"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = stringResource(R.string.gamble_double_half),
                fontFamily = OswaldFamily,
                fontWeight = FontWeight.Bold,
                color = ChampionshipGold,
              )
            }
          }

          Button(
            onClick = onCollect,
            enabled = !state.isFlipping,
            modifier = Modifier.weight(1.2f).heightIn(min = 48.dp).testTag("gamble_collect_button"),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = if (state.currentWinCents + state.bankedCents > 0L) WinEmeraldDark else CabinetMetalDark
              ),
            shape = RoundedCornerShape(10.dp),
          ) {
            val totalReturn = state.currentWinCents + state.bankedCents
            Text(
              text =
                if (totalReturn > 0L) "COLLECT ${formatCredits(totalReturn)}"
                else "RETURN TO REELS",
              fontFamily = BebasNeueFamily,
              fontSize = 18.sp,
              color = CreamWhite,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun GambleMeter(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(8.dp))
        .background(ArenaObsidian)
        .border(1.dp, CabinetBevel, RoundedCornerShape(8.dp))
        .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = label,
      color = MutedSilver,
      fontFamily = OswaldFamily,
      fontSize = 10.sp,
    )
    Text(
      text = value,
      color = color,
      fontFamily = RobotoMonoFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
    )
  }
}

@Composable
private fun MiniCardBadge(card: GambleCard) {
  val suitColor = if (card.suit.isRed) Color(0xFFD32F2F) else Color(0xFF14171F)
  Box(
    modifier =
      Modifier.size(width = 36.dp, height = 48.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(CreamWhite)
        .border(1.dp, CabinetBevel, RoundedCornerShape(6.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "${card.rank}\n${card.suit.symbol}",
      color = suitColor,
      fontFamily = OswaldFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp,
      lineHeight = 12.sp,
      textAlign = TextAlign.Center,
    )
  }
}
