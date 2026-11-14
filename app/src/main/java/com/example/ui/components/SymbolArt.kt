package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.slot.model.SlotSymbol
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.BebasNeueFamily
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ChampionshipGoldDeep
import com.example.ui.theme.ChampionshipGoldRich
import com.example.ui.theme.CreamWhite
import com.example.ui.theme.RingsideBlue
import com.example.ui.theme.RingsideBlueDark
import com.example.ui.theme.RockyCrimson
import com.example.ui.theme.RockyCrimsonDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SlotSymbolTile(
  symbol: SlotSymbol,
  isHighlighted: Boolean,
  isStickyWild: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "highlight_pulse")
  val pulseAlpha by
    infiniteTransition.animateFloat(
      initialValue = 0.6f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
      label = "pulse_alpha",
    )

  val highlightScale by
    animateFloatAsState(
      targetValue = if (isHighlighted) 1.045f else 1f,
      animationSpec = spring(dampingRatio = 0.48f, stiffness = Spring.StiffnessMediumLow),
      label = "highlight_scale",
    )

  val borderColor =
    when {
      isHighlighted -> ChampionshipGold.copy(alpha = pulseAlpha)
      isStickyWild -> ChampionshipGoldRich
      else -> Color(0xFF21324A)
    }
  val borderWidth = if (isHighlighted || isStickyWild) 2.dp else 0.8.dp

  Box(
    modifier =
      modifier
        .graphicsLayer {
          scaleX = highlightScale
          scaleY = highlightScale
        }
        .clip(RoundedCornerShape(3.dp))
        .background(Color(0xE80A1322))
        .border(borderWidth, borderColor, RoundedCornerShape(3.dp)),
    contentAlignment = Alignment.Center,
  ) {
    when (symbol) {
      SlotSymbol.WILD_ROCKY ->
        RealisticPortraitTile(
          drawableRes = R.drawable.img_symbol_wild_boxer,
          contentDescription = stringResource(R.string.cd_wild_boxer),
          badgeText = if (isStickyWild) "STICKY WILD" else "WILD",
          badgeBg = RockyCrimsonDark,
          isGoldFrame = true,
        )
      SlotSymbol.APOLLO ->
        RealisticPortraitTile(
          drawableRes = R.drawable.img_symbol_apollo,
          contentDescription = stringResource(R.string.cd_apollo_portrait),
          badgeText = null,
          badgeBg = RingsideBlueDark,
          isGoldFrame = false,
        )
      SlotSymbol.CLUBBER ->
        RealisticPortraitTile(
          drawableRes = R.drawable.img_symbol_clubber,
          contentDescription = stringResource(R.string.cd_clubber_portrait),
          badgeText = null,
          badgeBg = RockyCrimsonDark,
          isGoldFrame = false,
        )
      SlotSymbol.DRAGO ->
        RealisticPortraitTile(
          drawableRes = R.drawable.img_symbol_drago,
          contentDescription = stringResource(R.string.cd_drago_portrait),
          badgeText = null,
          badgeBg = RockyCrimsonDark,
          isGoldFrame = false,
        )
      SlotSymbol.BUTKUS ->
        RealisticPortraitTile(
          drawableRes = R.drawable.img_symbol_butkus,
          contentDescription = stringResource(R.string.cd_butkus_portrait),
          badgeText = null,
          badgeBg = ArenaObsidian,
          isGoldFrame = false,
        )
      SlotSymbol.BONUS_RB -> RbBonusAmericanaTile()
      SlotSymbol.CARD_A -> CardRoyalAmericanaTile("A", accent = RockyCrimson)
      SlotSymbol.CARD_K -> CardRoyalAmericanaTile("K", accent = RingsideBlue)
      SlotSymbol.CARD_Q -> CardRoyalAmericanaTile("Q", accent = RockyCrimson)
      SlotSymbol.CARD_J -> CardRoyalAmericanaTile("J", accent = RingsideBlue)
      SlotSymbol.CARD_10 -> CardRoyalAmericanaTile("10", accent = ChampionshipGoldRich)
    }

    if (isHighlighted) {
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(ChampionshipGold.copy(alpha = 0.14f * pulseAlpha))
      )
    }
  }
}

@Composable
private fun RealisticPortraitTile(
  @DrawableRes drawableRes: Int,
  contentDescription: String,
  badgeText: String?,
  badgeBg: Color,
  isGoldFrame: Boolean,
) {
  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(id = drawableRes),
      contentDescription = contentDescription,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier =
        Modifier.fillMaxSize()
          .border(
            width = if (isGoldFrame) 1.5.dp else 1.dp,
            brush =
              if (isGoldFrame) {
                Brush.verticalGradient(listOf(ChampionshipGold, ChampionshipGoldDeep))
              } else {
                Brush.verticalGradient(listOf(Color(0x88D4AF37), Color(0x441B2B44)))
              },
            shape = RoundedCornerShape(3.dp),
          )
    )
    if (badgeText != null) {
      Box(
        modifier =
          Modifier.align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(badgeBg.copy(alpha = 0.9f))
            .border(0.6.dp, ChampionshipGold, RoundedCornerShape(2.dp))
            .padding(vertical = 1.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = badgeText,
          color = ChampionshipGold,
          fontFamily = BebasNeueFamily,
          fontSize = if (badgeText.length > 5) 9.sp else 11.sp,
          letterSpacing = 1.sp,
          lineHeight = 11.sp,
        )
      }
    }
  }
}

@Composable
private fun RbBonusAmericanaTile() {
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.radialGradient(
            listOf(Color(0xFF1A2A4F), Color(0xFF0A101E))
          )
        )
        .border(
          2.2.dp,
          Brush.linearGradient(listOf(ChampionshipGold, RockyCrimson, CreamWhite, ChampionshipGold)),
          RoundedCornerShape(3.dp),
        ),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      // Top & bottom subtle Americana red/white/blue trim bars
      drawRect(color = RockyCrimson.copy(alpha = 0.85f), topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(w, h * 0.08f))
      drawRect(color = RingsideBlue.copy(alpha = 0.9f), topLeft = Offset(0f, h * 0.92f), size = androidx.compose.ui.geometry.Size(w, h * 0.08f))
      drawStar(Offset(w * 0.16f, h * 0.22f), w * 0.05f, ChampionshipGold.copy(alpha = 0.85f))
      drawStar(Offset(w * 0.84f, h * 0.22f), w * 0.05f, ChampionshipGold.copy(alpha = 0.85f))
    }

    Box(
      modifier =
        Modifier.fillMaxSize()
          .padding(4.dp)
          .border(1.dp, ChampionshipGoldRich.copy(alpha = 0.7f), RoundedCornerShape(2.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "RB",
        color = ArenaObsidian,
        fontFamily = BebasNeueFamily,
        fontSize = 34.sp,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 2.dp, start = 2.dp),
      )
      Text(
        text = "RB",
        color = ChampionshipGold,
        fontFamily = BebasNeueFamily,
        fontSize = 34.sp,
        letterSpacing = 1.5.sp,
      )
    }
  }
}

@Composable
private fun CardRoyalAmericanaTile(rank: String, accent: Color) {
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(listOf(Color(0xFF101D36), Color(0xFF070D19)))
        ),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      // Crisp thin red/blue Americana top accent stripe
      drawRect(
        color = accent.copy(alpha = 0.75f),
        topLeft = Offset(w * 0.12f, h * 0.05f),
        size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.04f),
      )
      // Subtle star watermark
      drawStar(Offset(w * 0.50f, h * 0.50f), w * 0.22f, Color.White.copy(alpha = 0.045f))
    }

    Text(
      text = rank,
      color = Color(0xFF03060C),
      fontFamily = BebasNeueFamily,
      fontSize = if (rank == "10") 34.sp else 38.sp,
      letterSpacing = if (rank == "10") (-1).sp else 0.5.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 3.dp, start = 2.dp),
    )
    Text(
      text = rank,
      color = CreamWhite,
      fontFamily = BebasNeueFamily,
      fontSize = if (rank == "10") 34.sp else 38.sp,
      letterSpacing = if (rank == "10") (-1).sp else 0.5.sp,
      textAlign = TextAlign.Center,
    )
  }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
  val path = Path()
  val innerRadius = radius * 0.42f
  for (i in 0 until 10) {
    val angle = Math.toRadians((-90 + i * 36).toDouble())
    val r = if (i % 2 == 0) radius else innerRadius
    val x = center.x + (r * cos(angle)).toFloat()
    val y = center.y + (r * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  drawPath(path = path, color = color)
}
