package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.slot.engine.RockySlotEngine
import com.example.slot.engine.formatCredits
import com.example.slot.model.PaylineDefinition
import com.example.slot.model.RockyMovieFeature
import com.example.slot.model.SlotStats
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
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.WinEmerald

@Composable
fun PaytableModal(
  stats: SlotStats,
  onDismiss: () -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("PAYTABLE", "FEATURES", "40 LINES", "STATS")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(ArenaObsidian.copy(alpha = 0.92f))
          .padding(14.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier =
          Modifier.widthIn(max = 580.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
            .clip(RoundedCornerShape(16.dp))
            .background(ArenaDeepSlate)
            .border(2.dp, ChampionshipGoldRich, RoundedCornerShape(16.dp))
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .background(ArenaObsidian)
              .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.paytable_title),
            color = ChampionshipGold,
            fontFamily = BebasNeueFamily,
            fontSize = 26.sp,
            letterSpacing = 1.5.sp,
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(48.dp).testTag("close_paytable_button"),
          ) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = CreamWhite)
          }
        }

        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = CabinetMetalDark,
          contentColor = ChampionshipGold,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = ChampionshipGold,
            )
          },
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  fontFamily = OswaldFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                )
              },
            )
          }
        }

        when (selectedTab) {
          0 -> PaytableSymbolsTab()
          1 -> FeaturesRulesTab()
          2 -> PaylinesDiagramTab()
          else -> StatsTab(stats)
        }
      }
    }
  }
}

@Composable
private fun PaytableSymbolsTab() {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(SlotSymbol.entries) { symbol ->
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CabinetMetalDark)
            .border(1.dp, CabinetBevel, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        SlotSymbolTile(
          symbol = symbol,
          isHighlighted = false,
          modifier = Modifier.size(58.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = symbol.displayName.uppercase(),
            color = ChampionshipGold,
            fontFamily = OswaldFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
          )
          val note =
            when {
              symbol.isWild -> "Substitutes for all regular symbols (x coin value)"
              symbol.isBonusRb -> "3+ anywhere triggers Rocky Movie Feature Hub (x Total Bet)"
              else -> "Pays left-to-right across 40 fixed paylines (x coin value)"
            }
          Text(
            text = note,
            color = MutedSilver,
            fontFamily = OswaldFamily,
            fontSize = 11.sp,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(5, 4, 3).forEach { count ->
              val mult = symbol.payoutsByCount[count]
              if (mult != null) {
                Text(
                  text = "${count}x = ${"%,d".format(mult)}",
                  color = if (count == 5) WinEmerald else CreamWhite,
                  fontFamily = RobotoMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
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
private fun FeaturesRulesTab() {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      FeatureInfoCard(
        title = "5×5 REELS • 40 FIXED PAYLINES",
        body =
          "Match 3, 4, or 5 identical symbols left-to-right along any of the 40 fixed paylines. Rocky Balboa (Wild) substitutes for all symbols except RB Bonus.",
      )
    }
    item {
      FeatureInfoCard(
        title = "RB BONUS & BUY BONUS (50× BET)",
        body =
          "Land 3 or more gold RB symbols anywhere on the 5×5 grid—or tap BUY BONUS—to unlock the Rocky Movie Feature Selector!",
      )
    }
    items(RockyMovieFeature.entries) { feature ->
      FeatureInfoCard(
        title = "ROCKY ${feature.numeral}: ${feature.title}",
        body = feature.subtitle,
      )
    }
  }
}

@Composable
private fun FeatureInfoCard(title: String, body: String) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(CabinetMetalDark)
        .border(1.dp, ChampionshipGoldRich.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = title,
      color = ChampionshipGold,
      fontFamily = OswaldFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 14.sp,
    )
    Text(
      text = body,
      color = CreamWhite,
      fontFamily = OswaldFamily,
      fontSize = 12.5.sp,
      lineHeight = 17.sp,
    )
  }
}

@Composable
private fun PaylinesDiagramTab() {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 96.dp),
    modifier = Modifier.fillMaxSize().padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(RockySlotEngine.PAYLINES) { line -> MiniPayline5x5Card(line) }
  }
}

@Composable
private fun MiniPayline5x5Card(line: PaylineDefinition) {
  Column(
    modifier =
      Modifier.clip(RoundedCornerShape(8.dp))
        .background(CabinetMetalDark)
        .border(1.dp, CabinetBevel, RoundedCornerShape(8.dp))
        .padding(6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = "LINE ${line.number}",
      color = line.color,
      fontFamily = OswaldFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp,
    )
    Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
      val cellW = size.width / 5f
      val cellH = size.height / 5f
      for (col in 0 until 5) {
        for (row in 0 until 5) {
          val isOnLine = line.rows[col] == row
          drawRect(
            color = if (isOnLine) line.color else Color(0xFF1F2638),
            topLeft = Offset(col * cellW + 1.5f, row * cellH + 1.5f),
            size = Size(cellW - 3f, cellH - 3f),
          )
        }
      }
    }
  }
}

@Composable
private fun StatsTab(stats: SlotStats) {
  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    StatRow("TOTAL SPINS PLAYED", stats.totalSpins.toString())
    StatRow("BIGGEST SINGLE WIN", formatCredits(stats.biggestWinCents))
    StatRow("BONUS FEATURES TRIGGERED", stats.rockyFeaturesHit.toString())
    StatRow("FREE GAMES SPINS PLAYED", stats.freeGamesTriggered.toString())
  }
}

@Composable
private fun StatRow(label: String, value: String) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 52.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(CabinetMetalDark)
        .border(1.dp, CabinetBevel, RoundedCornerShape(10.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      color = MutedSilver,
      fontFamily = OswaldFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
    )
    Text(
      text = value,
      color = ChampionshipGold,
      fontFamily = RobotoMonoFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
    )
  }
}
