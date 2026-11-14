package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slot.viewmodel.RockySlotViewModel
import com.example.ui.screen.RockySlotScreen
import com.example.ui.theme.RockySlotTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    setContent {
      RockySlotTheme {
        val slotViewModel: RockySlotViewModel = viewModel()
        RockySlotScreen(viewModel = slotViewModel)
      }
    }
  }
}
