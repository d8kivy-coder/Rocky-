package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.RingsideBlueDark
import com.example.ui.theme.RockyCrimsonDark

/**
 * Renders the realistic painted Philadelphia steps & Americana red-white-blue spotlight backdrop
 * with subtle stadium vignette for crystal-clear foreground cabinet contrast.
 */
@Composable
fun PhiladelphiaStepsBackdrop(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize().background(ArenaObsidian)) {
    Image(
      painter = painterResource(id = R.drawable.img_americana_steps),
      contentDescription = stringResource(R.string.cd_arena_backdrop),
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Subtle Americana Red-White-Blue atmospheric gradient wash & bottom contrast scrim
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      drawRect(
        brush =
          Brush.horizontalGradient(
            colors =
              listOf(
                RingsideBlueDark.copy(alpha = 0.42f),
                Color.Transparent,
                RockyCrimsonDark.copy(alpha = 0.34f),
              )
          )
      )

      // Top & bottom contrast vignette
      drawRect(
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                Color(0x99060A14),
                Color(0x18060A14),
                Color(0xCC060A14),
              ),
            startY = 0f,
            endY = h,
          )
      )

      // Soft warm golden spotlight glow behind the reel cabinet center
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0x28FFD54F), Color.Transparent),
            center = Offset(w * 0.50f, h * 0.46f),
            radius = w * 0.45f,
          ),
        center = Offset(w * 0.50f, h * 0.46f),
        radius = w * 0.45f,
      )
    }
  }
}
