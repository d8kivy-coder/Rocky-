package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slot.engine.formatCredits
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.BebasNeueFamily
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ChampionshipGoldDeep
import com.example.ui.theme.ChampionshipGoldRich
import com.example.ui.theme.CreamWhite
import com.example.ui.theme.OswaldFamily
import com.example.ui.theme.RingsideBlue
import com.example.ui.theme.RingsideBlueDark
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RockyCrimson
import com.example.ui.theme.RockyCrimsonDark
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WinCelebrationState(
  val title: String,
  val subtitle: String,
  val targetWinCents: Long,
  val isMega: Boolean,
)

private data class ConfettiParticle(
  val startX: Float,
  val startY: Float,
  val vx: Float,
  val vy: Float,
  val size: Float,
  val color: Color,
  val isCoin: Boolean,
  val spinSpeed: Float,
)

@Composable
fun WinCelebrationOverlay(
  celebration: WinCelebrationState,
  onDismiss: () -> Unit,
) {
  val bannerScale = remember { Animatable(0.35f) }
  val countAnim = remember { Animatable(0f) }
  val timeAnim = remember { Animatable(0f) }

  val particles =
    remember(celebration) {
      val palette = listOf(RockyCrimson, CreamWhite, RingsideBlue, ChampionshipGold, ChampionshipGoldRich)
      val rng = Random(System.currentTimeMillis())
      List(65) { idx ->
        ConfettiParticle(
          startX = 0.15f + rng.nextFloat() * 0.70f,
          startY = -0.05f - rng.nextFloat() * 0.25f,
          vx = (rng.nextFloat() - 0.5f) * 0.35f,
          vy = 0.38f + rng.nextFloat() * 0.52f,
          size = 8f + rng.nextFloat() * 11f,
          color = if (idx % 3 == 0) ChampionshipGold else palette[idx % palette.size],
          isCoin = idx % 3 == 0,
          spinSpeed = (rng.nextFloat() - 0.5f) * 360f,
        )
      }
    }

  val infinite = rememberInfiniteTransition(label = "starburst")
  val rayRotation by
    infinite.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
      label = "ray_rot",
    )

  LaunchedEffect(celebration) {
    bannerScale.snapTo(0.35f)
    bannerScale.animateTo(
      targetValue = 1f,
      animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
    )
  }

  LaunchedEffect(celebration) {
    countAnim.snapTo(0f)
    countAnim.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
    )
  }

  LaunchedEffect(celebration) {
    timeAnim.snapTo(0f)
    timeAnim.animateTo(
      targetValue = 3.2f,
      animationSpec = tween(durationMillis = 3200, easing = LinearEasing),
    )
    onDismiss()
  }

  val displayedCents = (celebration.targetWinCents * countAnim.value).toLong()
  val t = timeAnim.value

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(ArenaObsidian.copy(alpha = 0.68f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        )
        .testTag("win_celebration_overlay"),
    contentAlignment = Alignment.Center,
  ) {
    // Full-screen Red/White/Blue/Gold Starburst Rays & Confetti/Coin Fountain
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val center = Offset(w / 2f, h / 2f)

      // Rotating Patriotic & Gold Starburst Rays behind Banner
      val rayCount = 12
      for (r in 0 until rayCount) {
        val angle = rayRotation + r * (360f / rayCount)
        val rayColor =
          when (r % 3) {
            0 -> ChampionshipGold.copy(alpha = 0.14f)
            1 -> RockyCrimson.copy(alpha = 0.12f)
            else -> RingsideBlue.copy(alpha = 0.14f)
          }
        rotate(degrees = angle, pivot = center) {
          val path =
            Path().apply {
              moveTo(center.x, center.y)
              lineTo(center.x - w * 0.09f, -h * 0.25f)
              lineTo(center.x + w * 0.09f, -h * 0.25f)
              close()
            }
          drawPath(path = path, color = rayColor)
        }
      }

      // Falling Confetti & Spinning Gold Coins
      particles.forEach { p ->
        val px = ((p.startX + p.vx * t) % 1.1f) * w
        val py = ((p.startY + p.vy * t) % 1.15f) * h
        if (py in -30f..(h + 30f)) {
          if (p.isCoin) {
            val wobble = kotlin.math.abs(cos(Math.toRadians((t * p.spinSpeed).toDouble())).toFloat()).coerceAtLeast(0.25f)
            drawOval(
              color = ChampionshipGold,
              topLeft = Offset(px - p.size * wobble, py - p.size),
              size = Size(p.size * 2f * wobble, p.size * 2f),
            )
            drawOval(
              color = ChampionshipGoldDeep,
              topLeft = Offset(px - p.size * 0.6f * wobble, py - p.size * 0.6f),
              size = Size(p.size * 1.2f * wobble, p.size * 1.2f),
            )
          } else {
            rotate(degrees = t * p.spinSpeed, pivot = Offset(px, py)) {
              drawRect(
                color = p.color,
                topLeft = Offset(px - p.size * 0.6f, py - p.size * 0.35f),
                size = Size(p.size * 1.2f, p.size * 0.7f),
              )
            }
          }
        }
      }
    }

    // Center Championship Celebration Banner
    Column(
      modifier =
        Modifier.widthIn(max = 400.dp)
          .fillMaxWidth(0.88f)
          .scale(bannerScale.value)
          .clip(RoundedCornerShape(18.dp))
          .background(
            Brush.verticalGradient(listOf(RingsideBlueDark, ArenaObsidian, RockyCrimsonDark))
          )
          .border(
            3.dp,
            Brush.linearGradient(listOf(ChampionshipGold, CreamWhite, RockyCrimson, ChampionshipGold)),
            RoundedCornerShape(18.dp),
          )
          .padding(horizontal = 20.dp, vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Americana Stars & Stripes Ribbon Header
      Row(
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
      ) {
        Box(modifier = Modifier.weight(1f).fillMaxSize().background(RockyCrimson))
        Box(modifier = Modifier.weight(1f).fillMaxSize().background(CreamWhite))
        Box(modifier = Modifier.weight(1f).fillMaxSize().background(RingsideBlue))
      }

      Text(
        text = "★  ${celebration.title}  ★",
        color = ChampionshipGold,
        fontFamily = BebasNeueFamily,
        fontSize = if (celebration.isMega) 40.sp else 34.sp,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center,
      )

      Text(
        text = celebration.subtitle,
        color = CreamWhite,
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.8.sp,
        textAlign = TextAlign.Center,
      )

      if (celebration.targetWinCents > 0L) {
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(10.dp))
              .background(ArenaObsidian.copy(alpha = 0.85f))
              .border(1.5.dp, ChampionshipGoldRich, RoundedCornerShape(10.dp))
              .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
          Text(
            text = formatCredits(displayedCents),
            color = ChampionshipGold,
            fontFamily = RobotoMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
          )
        }
      }

      Text(
        text = "TAP ANYWHERE TO CONTINUE",
        color = CreamWhite.copy(alpha = 0.72f),
        fontFamily = OswaldFamily,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
      )
    }
  }
}
