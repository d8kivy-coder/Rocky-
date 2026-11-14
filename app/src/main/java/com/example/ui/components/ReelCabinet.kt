package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slot.engine.RockySlotEngine
import com.example.slot.engine.formatCredits
import com.example.slot.model.SlotSymbol
import com.example.slot.model.SpinEvaluation
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ChampionshipGoldRich
import com.example.ui.theme.CreamWhite
import com.example.ui.theme.OswaldFamily
import com.example.ui.theme.RingsideBlue
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RockyCrimson
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun ReelCabinet(
  grid: List<List<SlotSymbol>>,
  reelSpinning: List<Boolean>,
  inFreeSpinsMode: Boolean,
  stickyWildPositions: Set<Pair<Int, Int>>,
  lastEvaluation: SpinEvaluation?,
  highlightedLineIndex: Int,
  modifier: Modifier = Modifier,
) {
  val activeWinningLine =
    lastEvaluation?.winningLines?.let { lines ->
      if (lines.isNotEmpty()) lines[highlightedLineIndex % lines.size] else null
    }

  val highlightedCells: Set<Pair<Int, Int>> =
    remember(lastEvaluation, highlightedLineIndex, reelSpinning) {
      if (reelSpinning.any { it } || lastEvaluation == null) {
        emptySet()
      } else {
        buildSet {
          activeWinningLine?.positions?.forEach { add(it) }
          if (lastEvaluation.rbCount >= 3) {
            lastEvaluation.rbPositions.forEach { add(it) }
          }
        }
      }
    }

  val activePaylineToDraw =
    if (!reelSpinning.any { it } && activeWinningLine != null) {
      RockySlotEngine.PAYLINES.getOrNull(activeWinningLine.lineNumber - 1)
    } else null

  // Americana Red-White-Blue & Gold Triple Cabinet Frame
  Box(
    modifier =
      modifier
        .aspectRatio(1f, matchHeightConstraintsFirst = false)
        .clip(RoundedCornerShape(6.dp))
        .background(
          if (inFreeSpinsMode) Brush.linearGradient(listOf(ChampionshipGold, RockyCrimson, RingsideBlue))
          else Brush.linearGradient(listOf(RockyCrimson, Color(0xFF9E1515), RingsideBlue))
        )
        .padding(3.5.dp)
        .background(CreamWhite)
        .padding(2.dp)
        .background(
          Brush.linearGradient(listOf(ChampionshipGold, ChampionshipGoldRich, ChampionshipGold))
        )
        .padding(1.5.dp)
        .background(Color(0xE0070D17)),
    contentAlignment = Alignment.Center,
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier.fillMaxSize().padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        for (reelIdx in 0 until 5) {
          ReelColumn5Row(
            reelIndex = reelIdx,
            targetSymbols = grid[reelIdx],
            isSpinning = reelSpinning[reelIdx],
            stickyRows =
              stickyWildPositions.filter { it.first == reelIdx }.map { it.second }.toSet(),
            highlightedRows =
              highlightedCells.filter { it.first == reelIdx }.map { it.second }.toSet(),
            modifier = Modifier.weight(1f).fillMaxHeight(),
          )
        }
      }

      if (activePaylineToDraw != null && activeWinningLine != null) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val colWidth = size.width / 5f
          val rowHeight = size.height / 5f
          val points =
            activePaylineToDraw.rows.mapIndexed { reelIdx, rowIdx ->
              Offset(
                x = colWidth * reelIdx + colWidth / 2f,
                y = rowHeight * rowIdx + rowHeight / 2f,
              )
            }

          val path = Path().apply {
            moveTo(0f, points.first().y)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, points.last().y)
          }

          // Outer glow
          drawPath(
            path = path,
            color = activePaylineToDraw.color.copy(alpha = 0.40f),
            style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round),
          )
          // Core line
          drawPath(
            path = path,
            color = activePaylineToDraw.color,
            style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
          )
          points.forEach { pt ->
            drawCircle(color = ArenaObsidian, radius = 8f, center = pt)
            drawCircle(color = activePaylineToDraw.color, radius = 5f, center = pt)
          }
        }

        // Floating Winning Payline Pill
        Box(
          modifier =
            Modifier.align(Alignment.TopCenter)
              .padding(top = 6.dp)
              .clip(RoundedCornerShape(50))
              .background(ArenaObsidian.copy(alpha = 0.90f))
              .border(1.5.dp, activePaylineToDraw.color, RoundedCornerShape(50))
              .padding(horizontal = 12.dp, vertical = 3.dp)
        ) {
          Text(
            text = "LINE ${activeWinningLine.lineNumber}  •  +${formatCredits(activeWinningLine.payoutCents)}",
            color = ChampionshipGold,
            fontFamily = RobotoMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
          )
        }
      }
    }
  }
}

@Composable
private fun ReelColumn5Row(
  reelIndex: Int,
  targetSymbols: List<SlotSymbol>,
  isSpinning: Boolean,
  stickyRows: Set<Int>,
  highlightedRows: Set<Int>,
  modifier: Modifier = Modifier,
) {
  val strip = remember(reelIndex) { RockySlotEngine.BASE_REEL_STRIPS[reelIndex] }

  // Continuous virtual cell scroll position (decreasing = reels spin downward smoothly!)
  var scrollPos by remember { mutableFloatStateOf(0f) }
  var isAnimatingReel by remember { mutableStateOf(false) }
  var stopAnchor by remember { mutableIntStateOf(0) }
  var hasLockRange by remember { mutableStateOf(false) }
  val settleAnim = remember { Animatable(0f) }

  LaunchedEffect(isSpinning) {
    if (isSpinning) {
      hasLockRange = false
      isAnimatingReel = true
      var lastNs = 0L
      val speedCellsPerSec = 22f
      while (true) {
        withFrameNanos { frameNs ->
          if (lastNs != 0L) {
            val dt = ((frameNs - lastNs) / 1_000_000_000f).coerceIn(0f, 0.05f)
            scrollPos -= speedCellsPerSec * dt
          }
          lastNs = frameNs
        }
      }
    } else if (isAnimatingReel) {
      // Smoothly decelerate into targetSymbols[0..4] at stopAnchor
      val targetStop = floor(scrollPos).toInt() - 4
      stopAnchor = targetStop
      hasLockRange = true

      val startPos = scrollPos
      val decelAnim = Animatable(startPos)
      decelAnim.animateTo(
        targetValue = targetStop.toFloat(),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
      ) {
        scrollPos = value
      }

      // Realistic mechanical bounce overshoot settle
      settleAnim.snapTo(0.18f)
      settleAnim.animateTo(
        targetValue = 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
      ) {
        scrollPos = targetStop.toFloat() + value
      }
      scrollPos = 0f
      hasLockRange = false
      isAnimatingReel = false
    }
  }

  BoxWithConstraints(
    modifier = modifier.clipToBounds().background(Color(0xD008101A))
  ) {
    val gapDp = 2.dp
    val totalGapDp = gapDp * 4
    val cellHeightDp = (maxHeight - totalGapDp) / 5f
    val stepPx = with(androidx.compose.ui.platform.LocalDensity.current) { (cellHeightDp + gapDp).toPx() }

    if (!isAnimatingReel && !isSpinning) {
      // Idle settled state (zero overhead, crisp testTags)
      androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(gapDp),
      ) {
        for (rowIdx in 0 until 5) {
          SlotSymbolTile(
            symbol = targetSymbols[rowIdx],
            isHighlighted = rowIdx in highlightedRows,
            isStickyWild = rowIdx in stickyRows,
            modifier =
              Modifier.fillMaxWidth()
                .weight(1f)
                .testTag("reel_${reelIndex}_row_${rowIdx}"),
          )
        }
      }
    } else {
      // Silky 60fps continuous vertical reel rendering
      val topVirtualIndex = floor(scrollPos).toInt()
      val fractionalCell = scrollPos - topVirtualIndex // [0f, 1f)
      val yOffsetPx = (-fractionalCell * stepPx).roundToInt()

      Box(modifier = Modifier.fillMaxSize().alpha(if (isSpinning) 0.94f else 1f)) {
        for (offsetRow in -1..5) {
          val virtualIdx = topVirtualIndex + offsetRow
          val sym =
            if (hasLockRange && virtualIdx in stopAnchor..(stopAnchor + 4)) {
              targetSymbols[virtualIdx - stopAnchor]
            } else {
              strip[virtualIdx.mod(strip.size)]
            }
          val sticky = offsetRow in stickyRows && !isSpinning
          val cellTopPx = (offsetRow * stepPx).roundToInt() + yOffsetPx

          SlotSymbolTile(
            symbol = sym,
            isHighlighted = false,
            isStickyWild = sticky,
            modifier =
              Modifier.fillMaxWidth()
                .height(cellHeightDp)
                .offset { IntOffset(0, cellTopPx) },
          )
        }
      }
    }
  }
}
