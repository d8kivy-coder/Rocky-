package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.slot.engine.KnockoutMatchEngine
import com.example.slot.engine.formatCredits
import com.example.slot.model.KnockoutBonusState
import com.example.slot.model.Opponent
import com.example.slot.model.RoundOutcome
import com.example.slot.model.RoundResult
import com.example.slot.model.SlotSymbol
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
import com.example.ui.theme.RockyCrimsonBright
import com.example.ui.theme.WinEmerald
import com.example.ui.theme.WinEmeraldDark

@Composable
fun KnockoutBonusDialog(
  state: KnockoutBonusState,
  onSelectOpponent: (Opponent) -> Unit,
  onFightRound: () -> Unit,
  onToggleAutoFight: () -> Unit,
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
          .padding(14.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 580.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ArenaDeepSlate)
            .border(2.5.dp, ChampionshipGoldRich, RoundedCornerShape(16.dp))
            .verticalScroll(rememberScrollState()),
      ) {
        // Ring Header Banner
        Box(modifier = Modifier.fillMaxWidth().height(125.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_knockout_ring),
            contentDescription = stringResource(R.string.cd_knockout_ring),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    listOf(ArenaObsidian.copy(alpha = 0.35f), ArenaDeepSlate)
                  )
                )
          )
          Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Text(
              text = stringResource(R.string.knockout_title),
              color = ChampionshipGold,
              fontFamily = BebasNeueFamily,
              fontSize = 34.sp,
              letterSpacing = 2.sp,
            )
            Text(
              text =
                "10-ROUND MATCH • EACH ROUND WON PAYS 3X (${formatCredits(state.triggerBetCents * KnockoutMatchEngine.ROUND_WIN_MULTIPLIER)})",
              color = CreamWhite,
              fontFamily = OswaldFamily,
              fontSize = 12.sp,
              letterSpacing = 0.8.sp,
              textAlign = TextAlign.Center,
            )
          }
        }

        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          if (state.selectedOpponent == null) {
            Text(
              text = stringResource(R.string.knockout_choose_opponent),
              color = ChampionshipGold,
              fontFamily = OswaldFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              letterSpacing = 1.sp,
            )
            Text(
              text =
                "Win a round to collect 3x Total Bet. Score a KNOCKOUT to win ALL remaining rounds!",
              color = MutedSilver,
              fontFamily = OswaldFamily,
              fontSize = 13.sp,
              textAlign = TextAlign.Center,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Opponent.entries.forEach { opponent ->
                OpponentSelectionCard(
                  opponent = opponent,
                  onClick = { onSelectOpponent(opponent) },
                )
              }
            }
          } else {
            val opponent = state.selectedOpponent
            val lastOutcome = state.rounds.lastOrNull()

            // Animated Ring Canvas
            BoxingRingCanvas(
              opponent = opponent,
              lastOutcome = lastOutcome,
              modifier =
                Modifier.fillMaxWidth()
                  .height(140.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.5.dp, CabinetBevel, RoundedCornerShape(12.dp)),
            )

            // 10-Round Scorecard Pills
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                for (r in 1..5) {
                  RoundScorePill(
                    roundNum = r,
                    outcome = state.rounds.getOrNull(r - 1),
                    modifier = Modifier.weight(1f),
                  )
                }
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                for (r in 6..10) {
                  RoundScorePill(
                    roundNum = r,
                    outcome = state.rounds.getOrNull(r - 1),
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }

            // Live Commentary & Prize Meter
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(ArenaObsidian)
                  .border(1.dp, CabinetBevel, RoundedCornerShape(10.dp))
                  .padding(12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text =
                  lastOutcome?.commentary
                    ?: "BELL RINGS! ROCKY FACES ${opponent.displayName.uppercase()} FOR 10 ROUNDS!",
                color =
                  if (lastOutcome?.result == RoundResult.ROCKY_KNOCKOUT) ChampionshipGold
                  else CreamWhite,
                fontFamily = OswaldFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
              )
            }

            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF0B2214))
                  .border(1.5.dp, WinEmerald, RoundedCornerShape(10.dp))
                  .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "TOTAL BONUS WON",
                color = CreamWhite,
                fontFamily = OswaldFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
              )
              Text(
                text = formatCredits(state.totalBonusWinCents),
                color = WinEmerald,
                fontFamily = RobotoMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
              )
            }

            // Action Buttons
            if (!state.isComplete) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Button(
                  onClick = onFightRound,
                  enabled = !state.isAutoFighting,
                  modifier = Modifier.weight(1.3f).heightIn(min = 50.dp).testTag("fight_round_button"),
                  colors = ButtonDefaults.buttonColors(containerColor = RockyCrimson),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Text(
                    text = "FIGHT ROUND ${state.currentRound + 1}",
                    fontFamily = BebasNeueFamily,
                    fontSize = 20.sp,
                    color = ChampionshipGold,
                    letterSpacing = 1.sp,
                  )
                }

                OutlinedButton(
                  onClick = onToggleAutoFight,
                  modifier = Modifier.weight(1f).heightIn(min = 50.dp).testTag("auto_fight_button"),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Text(
                    text = if (state.isAutoFighting) "PAUSE" else "AUTO FIGHT",
                    fontFamily = OswaldFamily,
                    fontWeight = FontWeight.Bold,
                    color = ChampionshipGold,
                  )
                }
              }
            } else {
              Button(
                onClick = onCollect,
                modifier =
                  Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("collect_knockout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = WinEmeraldDark),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(
                  text = "COLLECT ${formatCredits(state.totalBonusWinCents)} & RETURN",
                  fontFamily = BebasNeueFamily,
                  fontSize = 22.sp,
                  color = CreamWhite,
                  letterSpacing = 1.2.sp,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OpponentSelectionCard(opponent: Opponent, onClick: () -> Unit) {
  val symbol =
    when (opponent) {
      Opponent.APOLLO -> SlotSymbol.APOLLO
      Opponent.CLUBBER -> SlotSymbol.CLUBBER
      Opponent.DRAGO -> SlotSymbol.DRAGO
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 76.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(
          Brush.horizontalGradient(
            listOf(opponent.styleColor.copy(alpha = 0.65f), CabinetMetalDark)
          )
        )
        .border(1.5.dp, opponent.accentColor, RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .padding(10.dp)
        .testTag("select_opponent_${opponent.name.lowercase()}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    SlotSymbolTile(
      symbol = symbol,
      isHighlighted = false,
      modifier = Modifier.size(56.dp),
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = opponent.displayName.uppercase(),
        color = ChampionshipGold,
        fontFamily = BebasNeueFamily,
        fontSize = 22.sp,
        letterSpacing = 1.sp,
      )
      Text(
        text = "${opponent.nickname} • ${opponent.weightClass}",
        color = CreamWhite,
        fontFamily = OswaldFamily,
        fontSize = 12.sp,
      )
    }
    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(8.dp))
          .background(RockyCrimson)
          .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Text(
        text = "FIGHT",
        color = ChampionshipGold,
        fontFamily = BebasNeueFamily,
        fontSize = 16.sp,
      )
    }
  }
}

@Composable
private fun BoxingRingCanvas(
  opponent: Opponent,
  lastOutcome: RoundOutcome?,
  modifier: Modifier = Modifier,
) {
  val punchShift = remember { Animatable(0f) }
  LaunchedEffect(lastOutcome?.roundNumber, lastOutcome?.result) {
    if (lastOutcome != null) {
      punchShift.snapTo(if (lastOutcome.result == RoundResult.OPPONENT_WIN) -1f else 1f)
      punchShift.animateTo(0f, spring(dampingRatio = 0.45f))
    }
  }

  val opponentDown = lastOutcome?.result == RoundResult.ROCKY_KNOCKOUT

  Canvas(modifier = modifier.background(Color(0xFF0B0E17))) {
    val w = size.width
    val h = size.height

    // Ring mat & ropes
    drawRect(color = Color(0xFF1A243B), topLeft = Offset(0f, h * 0.65f), size = Size(w, h * 0.35f))
    val ropeY = listOf(h * 0.25f, h * 0.40f, h * 0.55f)
    val ropeColors = listOf(RockyCrimson, CreamWhite, RingsideBlue)
    ropeY.forEachIndexed { idx, y ->
      drawLine(color = ropeColors[idx], start = Offset(0f, y), end = Offset(w, y), strokeWidth = 4f)
    }

    // Rocky (Left corner)
    val rockyX = w * 0.34f + punchShift.value * w * 0.10f
    drawCircle(color = Color(0xFFE0AC8B), radius = h * 0.11f, center = Offset(rockyX, h * 0.34f))
    drawRoundRect(
      color = Color(0xFFD4A080),
      topLeft = Offset(rockyX - w * 0.05f, h * 0.44f),
      size = Size(w * 0.10f, h * 0.28f),
    )
    // Rocky's red glove
    drawCircle(
      color = RockyCrimsonBright,
      radius = h * 0.075f,
      center = Offset(rockyX + w * 0.09f, h * 0.46f),
    )

    // Opponent (Right corner)
    if (opponentDown) {
      // Knocked down on mat!
      drawRoundRect(
        color = opponent.styleColor,
        topLeft = Offset(w * 0.56f, h * 0.72f),
        size = Size(w * 0.24f, h * 0.10f),
      )
      drawCircle(color = opponent.accentColor, radius = h * 0.08f, center = Offset(w * 0.82f, h * 0.76f))
    } else {
      val oppX = w * 0.66f + punchShift.value * w * 0.06f
      drawCircle(color = opponent.accentColor, radius = h * 0.11f, center = Offset(oppX, h * 0.34f))
      drawRoundRect(
        color = opponent.styleColor,
        topLeft = Offset(oppX - w * 0.05f, h * 0.44f),
        size = Size(w * 0.10f, h * 0.28f),
      )
      // Opponent's glove
      drawCircle(
        color = CreamWhite,
        radius = h * 0.075f,
        center = Offset(oppX - w * 0.09f, h * 0.46f),
      )
    }

    // Impact starburst
    if (punchShift.value != 0f) {
      drawCircle(
        color = ChampionshipGold.copy(alpha = kotlin.math.abs(punchShift.value) * 0.65f),
        radius = h * 0.16f,
        center = Offset(w * 0.50f, h * 0.42f),
      )
    }
  }
}

@Composable
private fun RoundScorePill(
  roundNum: Int,
  outcome: RoundOutcome?,
  modifier: Modifier = Modifier,
) {
  val (bg, border, textColor, badge) =
    when (outcome?.result) {
      RoundResult.ROCKY_KNOCKOUT ->
        listOf(Color(0xFF4A3500), ChampionshipGold, ChampionshipGold, "KO!")
      RoundResult.ROCKY_WIN ->
        listOf(WinEmeraldDark.copy(alpha = 0.7f), WinEmerald, CreamWhite, "WIN")
      RoundResult.OPPONENT_WIN ->
        listOf(RockyCrimson.copy(alpha = 0.55f), RockyCrimsonBright, CreamWhite, "LOSS")
      null ->
        listOf(CabinetMetalDark, CabinetBevel, MutedSilver, "—")
    }

  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(8.dp))
        .background(bg as Color)
        .border(1.2.dp, border as Color, RoundedCornerShape(8.dp))
        .padding(vertical = 5.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = "R$roundNum",
      color = MutedSilver,
      fontFamily = OswaldFamily,
      fontSize = 10.sp,
    )
    Text(
      text = badge as String,
      color = textColor as Color,
      fontFamily = BebasNeueFamily,
      fontSize = 14.sp,
    )
  }
}
