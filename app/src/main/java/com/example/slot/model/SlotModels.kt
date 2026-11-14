package com.example.slot.model

import androidx.compose.ui.graphics.Color

enum class SlotSymbol(
  val id: String,
  val displayName: String,
  val payoutsByCount: Map<Int, Int>,
  val isWild: Boolean = false,
  val isBonusRb: Boolean = false,
) {
  WILD_ROCKY(
    id = "wild_rocky",
    displayName = "Rocky Balboa (Wild)",
    payoutsByCount = mapOf(3 to 50, 4 to 200, 5 to 1000),
    isWild = true,
  ),
  BONUS_RB(
    id = "bonus_rb",
    displayName = "RB Bonus",
    payoutsByCount = mapOf(3 to 3, 4 to 10, 5 to 50), // x Total Bet
    isBonusRb = true,
  ),
  APOLLO(
    id = "apollo",
    displayName = "Apollo Creed",
    payoutsByCount = mapOf(3 to 30, 4 to 120, 5 to 500),
  ),
  CLUBBER(
    id = "clubber",
    displayName = "Clubber Lang",
    payoutsByCount = mapOf(3 to 25, 4 to 100, 5 to 400),
  ),
  DRAGO(
    id = "drago",
    displayName = "Ivan Drago",
    payoutsByCount = mapOf(3 to 20, 4 to 80, 5 to 300),
  ),
  BUTKUS(
    id = "butkus",
    displayName = "Butkus",
    payoutsByCount = mapOf(3 to 15, 4 to 60, 5 to 200),
  ),
  CARD_A(
    id = "card_a",
    displayName = "Ace (A)",
    payoutsByCount = mapOf(3 to 10, 4 to 30, 5 to 100),
  ),
  CARD_K(
    id = "card_k",
    displayName = "King (K)",
    payoutsByCount = mapOf(3 to 8, 4 to 25, 5 to 80),
  ),
  CARD_Q(
    id = "card_q",
    displayName = "Queen (Q)",
    payoutsByCount = mapOf(3 to 6, 4 to 20, 5 to 60),
  ),
  CARD_J(
    id = "card_j",
    displayName = "Jack (J)",
    payoutsByCount = mapOf(3 to 5, 4 to 15, 5 to 50),
  ),
  CARD_10(
    id = "card_10",
    displayName = "Ten (10)",
    payoutsByCount = mapOf(3 to 5, 4 to 15, 5 to 50),
  );

  val isRegularPaySymbol: Boolean
    get() = !isBonusRb
}

data class PaylineDefinition(val number: Int, val rows: List<Int>, val color: Color)

data class WinningLine(
  val lineNumber: Int,
  val symbol: SlotSymbol,
  val matchCount: Int,
  val positions: List<Pair<Int, Int>>,
  val payoutCents: Long,
)

data class SpinEvaluation(
  val grid: List<List<SlotSymbol>>,
  val winningLines: List<WinningLine>,
  val rbCount: Int,
  val rbPositions: List<Pair<Int, Int>>,
  val rbScatterPayoutCents: Long,
  val bonusTriggered: Boolean,
  val multiplierApplied: Int,
  val totalWinCents: Long,
) {
  val hasAnyWin: Boolean
    get() = totalWinCents > 0L || bonusTriggered
}

enum class RockyMovieFeature(
  val numeral: String,
  val title: String,
  val subtitle: String,
  val accentColor: Color,
) {
  ROCKY_I("I", "APOLLO FREE GAMES", "8 Free Spins with Sticky Wilds & 2x Wins", Color(0xFF1E88E5)),
  ROCKY_II("II", "HEAVYWEIGHT GLOVE PICK", "Pick Boxing Gloves for Cash Multipliers", Color(0xFFD4AF37)),
  ROCKY_III("III", "CLUBBER STACKED SPINS", "8 Free Spins with Stacked Champions", Color(0xFFE53935)),
  ROCKY_IV("IV", "DRAGO HOLD & RESPIN", "Lock Golden RB Badges for Prize Streaks", Color(0xFFFF7043)),
  ROCKY_V("V", "CHAMPIONSHIP WHEEL", "Spin the 3-Tier Multiplier Wheel", Color(0xFFAB47BC)),
  ROCKY_RB("RB", "PHILADELPHIA TRAINING", "Steps Climb up to 100x Total Bet", Color(0xFF00E676)),
}

enum class Opponent(
  val displayName: String,
  val nickname: String,
  val weightClass: String,
  val styleColor: Color,
  val accentColor: Color,
  val winRoundProbability: Float,
  val koProbability: Float,
) {
  APOLLO(
    displayName = "Apollo Creed",
    nickname = "The Master of Disaster",
    weightClass = "Heavyweight Champion",
    styleColor = Color(0xFF15439C),
    accentColor = Color(0xFFE53935),
    winRoundProbability = 0.62f,
    koProbability = 0.14f,
  ),
  CLUBBER(
    displayName = "Clubber Lang",
    nickname = "The Southside Slugger",
    weightClass = "No. 1 Contender",
    styleColor = Color(0xFF2B2118),
    accentColor = Color(0xFFFFD54F),
    winRoundProbability = 0.56f,
    koProbability = 0.17f,
  ),
  DRAGO(
    displayName = "Ivan Drago",
    nickname = "The Siberian Express",
    weightClass = "Olympic Gold Giant",
    styleColor = Color(0xFF8B0000),
    accentColor = Color(0xFFFFB300),
    winRoundProbability = 0.52f,
    koProbability = 0.20f,
  ),
}

enum class RoundResult {
  ROCKY_WIN,
  OPPONENT_WIN,
  ROCKY_KNOCKOUT,
}

data class RoundOutcome(
  val roundNumber: Int,
  val result: RoundResult,
  val rewardCents: Long,
  val commentary: String,
)

data class KnockoutBonusState(
  val triggerBetCents: Long,
  val selectedOpponent: Opponent? = null,
  val rounds: List<RoundOutcome> = emptyList(),
  val currentRound: Int = 0,
  val totalBonusWinCents: Long = 0L,
  val isComplete: Boolean = false,
  val isAutoFighting: Boolean = false,
)

data class MovieBonusHubState(
  val triggerBetCents: Long,
  val purchasedWithBuyBonus: Boolean,
  val pickedFeature: RockyMovieFeature? = null,
  val wheelSpinning: Boolean = false,
  val glovePicksRemaining: Int = 3,
  val revealedGloveMultipliers: List<Int?> = List(6) { null },
  val trainingStep: Int = 0,
  val featureAwardCents: Long = 0L,
  val isResolved: Boolean = false,
)

enum class CardSuit(val symbol: String, val isRed: Boolean) {
  HEARTS("♥", true),
  DIAMONDS("♦", true),
  CLUBS("♣", false),
  SPADES("♠", false),
}

enum class CardColor {
  RED,
  BLACK,
}

data class GambleCard(val rank: String, val suit: CardSuit) {
  val color: CardColor
    get() = if (suit.isRed) CardColor.RED else CardColor.BLACK
}

data class GambleState(
  val currentWinCents: Long,
  val bankedCents: Long = 0L,
  val history: List<GambleCard> = emptyList(),
  val lastDrawnCard: GambleCard? = null,
  val lastGuessWon: Boolean? = null,
  val isFlipping: Boolean = false,
  val isFinished: Boolean = false,
)

data class SlotStats(
  val totalSpins: Int = 0,
  val biggestWinCents: Long = 0L,
  val knockoutsWon: Int = 0,
  val freeGamesTriggered: Int = 0,
  val rockyFeaturesHit: Int = 0,
)
