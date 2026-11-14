package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.slot.engine.formatCredits
import com.example.slot.model.MovieBonusHubState
import com.example.slot.model.RockyMovieFeature
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
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RockyCrimson
import com.example.ui.theme.WinEmerald
import com.example.ui.theme.WinEmeraldDark

@Composable
fun MovieBonusHubDialog(
  state: MovieBonusHubState,
  onSelectFeature: (RockyMovieFeature) -> Unit,
  onPickGlove: (Int) -> Unit,
  onSpinWheel: () -> Unit,
  onAdvanceTraining: () -> Unit,
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
          Modifier.widthIn(max = 560.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ArenaDeepSlate)
            .border(2.5.dp, ChampionshipGoldRich, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        val picked = state.pickedFeature
        if (picked == null) {
          Text(
            text = if (state.purchasedWithBuyBonus) "BUY BONUS — CHOOSE FEATURE" else "RB BONUS TRIGGERED!",
            color = ChampionshipGold,
            fontFamily = BebasNeueFamily,
            fontSize = 30.sp,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
          )
          Text(
            text = "SELECT ANY ROCKY CHAMPIONSHIP FEATURE",
            color = MutedSilver,
            fontFamily = OswaldFamily,
            fontSize = 13.sp,
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RockyMovieFeature.entries.forEach { feature ->
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                      Brush.horizontalGradient(
                        listOf(feature.accentColor.copy(alpha = 0.28f), CabinetMetalDark)
                      )
                    )
                    .border(1.5.dp, feature.accentColor, RoundedCornerShape(10.dp))
                    .clickable { onSelectFeature(feature) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("feature_${feature.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Box(
                  modifier =
                    Modifier.size(44.dp)
                      .clip(CircleShape)
                      .background(ArenaObsidian)
                      .border(1.5.dp, feature.accentColor, CircleShape),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = feature.numeral,
                    color = ChampionshipGold,
                    fontFamily = BebasNeueFamily,
                    fontSize = 20.sp,
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = feature.title,
                    color = CreamWhite,
                    fontFamily = OswaldFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                  )
                  Text(
                    text = feature.subtitle,
                    color = MutedSilver,
                    fontFamily = OswaldFamily,
                    fontSize = 12.sp,
                  )
                }
                Text(
                  text = "PLAY",
                  color = feature.accentColor,
                  fontFamily = BebasNeueFamily,
                  fontSize = 18.sp,
                )
              }
            }
          }
        } else {
          when (picked) {
            RockyMovieFeature.ROCKY_II -> {
              Text(
                text = "ROCKY II • HEAVYWEIGHT GLOVE PICK",
                color = ChampionshipGold,
                fontFamily = BebasNeueFamily,
                fontSize = 26.sp,
              )
              Text(
                text = "PICKS REMAINING: ${state.glovePicksRemaining}",
                color = CreamWhite,
                fontFamily = OswaldFamily,
                fontSize = 14.sp,
              )
              LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                itemsIndexed(state.revealedGloveMultipliers) { idx, mult ->
                  Box(
                    modifier =
                      Modifier.heightIn(min = 84.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (mult != null) Color(0xFF112A18) else RockyCrimson.copy(alpha = 0.7f))
                        .border(
                          1.5.dp,
                          if (mult != null) WinEmerald else ChampionshipGold,
                          RoundedCornerShape(10.dp),
                        )
                        .clickable(enabled = mult == null && state.glovePicksRemaining > 0) {
                          onPickGlove(idx)
                        },
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = if (mult != null) "${mult}X" else "🥊\nPICK",
                      color = if (mult != null) ChampionshipGold else CreamWhite,
                      fontFamily = BebasNeueFamily,
                      fontSize = if (mult != null) 26.sp else 16.sp,
                      textAlign = TextAlign.Center,
                    )
                  }
                }
              }
            }

            RockyMovieFeature.ROCKY_V -> {
              Text(
                text = "ROCKY V • CHAMPIONSHIP WHEEL",
                color = ChampionshipGold,
                fontFamily = BebasNeueFamily,
                fontSize = 26.sp,
              )
              Box(
                modifier =
                  Modifier.size(140.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF4A148C), ArenaObsidian)))
                    .border(4.dp, ChampionshipGold, CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text =
                    when {
                      state.wheelSpinning -> "SPINNING..."
                      state.isResolved -> "${state.trainingStep}X"
                      else -> "SPIN"
                    },
                  color = ChampionshipGold,
                  fontFamily = BebasNeueFamily,
                  fontSize = if (state.isResolved) 38.sp else 24.sp,
                )
              }
              if (!state.isResolved) {
                Button(
                  onClick = onSpinWheel,
                  enabled = !state.wheelSpinning,
                  colors = ButtonDefaults.buttonColors(containerColor = RockyCrimson),
                  modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                  Text(
                    text = if (state.wheelSpinning) "SPINNING..." else "SPIN CHAMPIONSHIP WHEEL",
                    fontFamily = BebasNeueFamily,
                    fontSize = 20.sp,
                    color = ChampionshipGold,
                  )
                }
              }
            }

            RockyMovieFeature.ROCKY_RB -> {
              Text(
                text = "RB • PHILADELPHIA STEPS TRAINING",
                color = ChampionshipGold,
                fontFamily = BebasNeueFamily,
                fontSize = 26.sp,
              )
              Text(
                text = "CLIMB THE STEPS WITH BUTKUS FOR BIGGER MULTIPLIERS (STEP ${state.trainingStep}/10)",
                color = CreamWhite,
                fontFamily = OswaldFamily,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                for (step in 1..10) {
                  val reached = step <= state.trainingStep
                  Box(
                    modifier =
                      Modifier.weight(1f)
                        .heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (reached) WinEmeraldDark else CabinetMetalDark)
                        .border(1.dp, if (reached) WinEmerald else CabinetBevel, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      text = step.toString(),
                      color = if (reached) CreamWhite else MutedSilver,
                      fontFamily = OswaldFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                    )
                  }
                }
              }
              if (!state.isResolved) {
                Button(
                  onClick = onAdvanceTraining,
                  colors = ButtonDefaults.buttonColors(containerColor = RockyCrimson),
                  modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                  Text(
                    text = "RUN NEXT STEPS!",
                    fontFamily = BebasNeueFamily,
                    fontSize = 20.sp,
                    color = ChampionshipGold,
                  )
                }
              }
            }

            else -> Unit
          }

          // Reward summary + Collect button
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ArenaObsidian)
                .border(1.5.dp, WinEmerald, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "FEATURE PRIZE",
              color = CreamWhite,
              fontFamily = OswaldFamily,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = formatCredits(state.featureAwardCents),
              color = WinEmerald,
              fontFamily = RobotoMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
            )
          }

          if (state.isResolved || (picked == RockyMovieFeature.ROCKY_RB && state.featureAwardCents > 0L)) {
            Button(
              onClick = onCollect,
              colors = ButtonDefaults.buttonColors(containerColor = WinEmeraldDark),
              modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("collect_movie_bonus_button"),
            ) {
              Text(
                text = "COLLECT ${formatCredits(state.featureAwardCents)}",
                fontFamily = BebasNeueFamily,
                fontSize = 22.sp,
                color = CreamWhite,
              )
            }
          }
        }
      }
    }
  }
}
