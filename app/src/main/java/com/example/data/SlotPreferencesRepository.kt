package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.slot.model.SlotStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.slotDataStore: DataStore<Preferences> by preferencesDataStore(name = "rocky_slot_prefs")

data class PersistedSlotData(
  val balanceCents: Long = DEFAULT_BALANCE_CENTS,
  val totalBetCents: Long = 500L, // 5.00 default matching original screenshot
  val soundEnabled: Boolean = true,
  val turboEnabled: Boolean = false,
  val stats: SlotStats = SlotStats(),
) {
  companion object {
    const val DEFAULT_BALANCE_CENTS = 1_000_000L // 10,000.00 Play-For-Free credits
  }
}

class SlotPreferencesRepository(private val context: Context) {

  private object Keys {
    val BALANCE_CENTS = longPreferencesKey("balance_cents")
    val TOTAL_BET_CENTS = longPreferencesKey("total_bet_cents")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val TURBO_ENABLED = booleanPreferencesKey("turbo_enabled")
    val TOTAL_SPINS = intPreferencesKey("total_spins")
    val BIGGEST_WIN_CENTS = longPreferencesKey("biggest_win_cents")
    val KNOCKOUTS_WON = intPreferencesKey("knockouts_won")
    val FREE_GAMES_TRIGGERED = intPreferencesKey("free_games_triggered")
    val ROCKY_FEATURES_HIT = intPreferencesKey("rocky_features_hit")
  }

  val slotData: Flow<PersistedSlotData> =
    context.slotDataStore.data.map { prefs ->
      PersistedSlotData(
        balanceCents = prefs[Keys.BALANCE_CENTS] ?: PersistedSlotData.DEFAULT_BALANCE_CENTS,
        totalBetCents = prefs[Keys.TOTAL_BET_CENTS] ?: 500L,
        soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
        turboEnabled = prefs[Keys.TURBO_ENABLED] ?: false,
        stats =
          SlotStats(
            totalSpins = prefs[Keys.TOTAL_SPINS] ?: 0,
            biggestWinCents = prefs[Keys.BIGGEST_WIN_CENTS] ?: 0L,
            knockoutsWon = prefs[Keys.KNOCKOUTS_WON] ?: 0,
            freeGamesTriggered = prefs[Keys.FREE_GAMES_TRIGGERED] ?: 0,
            rockyFeaturesHit = prefs[Keys.ROCKY_FEATURES_HIT] ?: 0,
          ),
      )
    }

  suspend fun saveBalance(balanceCents: Long) {
    context.slotDataStore.edit { prefs -> prefs[Keys.BALANCE_CENTS] = balanceCents.coerceAtLeast(0L) }
  }

  suspend fun saveTotalBet(totalBetCents: Long) {
    context.slotDataStore.edit { prefs -> prefs[Keys.TOTAL_BET_CENTS] = totalBetCents }
  }

  suspend fun saveToggles(soundEnabled: Boolean, turboEnabled: Boolean) {
    context.slotDataStore.edit { prefs ->
      prefs[Keys.SOUND_ENABLED] = soundEnabled
      prefs[Keys.TURBO_ENABLED] = turboEnabled
    }
  }

  suspend fun recordSpinStats(
    winCents: Long,
    hitBonus: Boolean,
    hitFreeGames: Boolean,
  ) {
    context.slotDataStore.edit { prefs ->
      val prevSpins = prefs[Keys.TOTAL_SPINS] ?: 0
      val prevBest = prefs[Keys.BIGGEST_WIN_CENTS] ?: 0L
      val prevKo = prefs[Keys.KNOCKOUTS_WON] ?: 0
      val prevFg = prefs[Keys.FREE_GAMES_TRIGGERED] ?: 0
      val prevRocky = prefs[Keys.ROCKY_FEATURES_HIT] ?: 0

      prefs[Keys.TOTAL_SPINS] = prevSpins + 1
      if (winCents > prevBest) prefs[Keys.BIGGEST_WIN_CENTS] = winCents
      if (hitBonus) {
        prefs[Keys.KNOCKOUTS_WON] = prevKo + 1
        prefs[Keys.ROCKY_FEATURES_HIT] = prevRocky + 1
      }
      if (hitFreeGames) prefs[Keys.FREE_GAMES_TRIGGERED] = prevFg + 1
    }
  }

  suspend fun updateBiggestWin(winCents: Long) {
    context.slotDataStore.edit { prefs ->
      val prevBest = prefs[Keys.BIGGEST_WIN_CENTS] ?: 0L
      if (winCents > prevBest) prefs[Keys.BIGGEST_WIN_CENTS] = winCents
    }
  }
}
