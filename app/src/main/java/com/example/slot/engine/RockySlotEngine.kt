package com.example.slot.engine

import androidx.compose.ui.graphics.Color
import com.example.slot.model.CardColor
import com.example.slot.model.CardSuit
import com.example.slot.model.GambleCard
import com.example.slot.model.Opponent
import com.example.slot.model.PaylineDefinition
import com.example.slot.model.RoundOutcome
import com.example.slot.model.RoundResult
import com.example.slot.model.SlotSymbol
import com.example.slot.model.SpinEvaluation
import com.example.slot.model.WinningLine
import kotlin.random.Random

object RockySlotEngine {

  const val GRID_COLS = 5
  const val GRID_ROWS = 5
  const val TOTAL_PAYLINES = 40
  const val BUY_BONUS_MULTIPLIER = 50L

  // Total Bet presets matching Playtech 40-line coin values (0.01 to 2.50 coin -> 0.40 to 100.00 bet, plus 5.00 default!)
  val TOTAL_BET_OPTIONS_CENTS: List<Long> =
    listOf(40L, 100L, 200L, 400L, 500L, 1000L, 2000L, 5000L, 10000L)

  private val PAYLINE_PALETTE =
    listOf(
      Color(0xFFFFD54F),
      Color(0xFFE53935),
      Color(0xFF2979FF),
      Color(0xFF00E676),
      Color(0xFFFF9100),
      Color(0xFF00E5FF),
      Color(0xFFE040FB),
      Color(0xFFFFEA00),
    )

  // 40 Fixed Paylines across a 5x5 grid (rows 0..4)
  val PAYLINES: List<PaylineDefinition> =
    listOf(
      listOf(2, 2, 2, 2, 2), // 1: Center straight
      listOf(0, 0, 0, 0, 0), // 2: Top straight
      listOf(4, 4, 4, 4, 4), // 3: Bottom straight
      listOf(1, 1, 1, 1, 1), // 4: Upper-mid straight
      listOf(3, 3, 3, 3, 3), // 5: Lower-mid straight
      listOf(0, 1, 2, 3, 4), // 6: Diagonal down
      listOf(4, 3, 2, 1, 0), // 7: Diagonal up
      listOf(0, 1, 2, 1, 0), // 8: Top V
      listOf(4, 3, 2, 3, 4), // 9: Bottom inverted V
      listOf(2, 1, 0, 1, 2), // 10: Mid-up peak
      listOf(2, 3, 4, 3, 2), // 11: Mid-down valley
      listOf(1, 2, 3, 2, 1), // 12: Inner V
      listOf(3, 2, 1, 2, 3), // 13: Inner peak
      listOf(0, 0, 1, 2, 2), // 14
      listOf(4, 4, 3, 2, 2), // 15
      listOf(2, 2, 1, 0, 0), // 16
      listOf(2, 2, 3, 4, 4), // 17
      listOf(1, 0, 0, 0, 1), // 18
      listOf(3, 4, 4, 4, 3), // 19
      listOf(0, 2, 4, 2, 0), // 20: Deep V
      listOf(4, 2, 0, 2, 4), // 21: Deep peak
      listOf(1, 0, 1, 2, 1), // 22
      listOf(3, 4, 3, 2, 3), // 23
      listOf(0, 1, 0, 1, 0), // 24
      listOf(4, 3, 4, 3, 4), // 25
      listOf(1, 2, 1, 2, 1), // 26
      listOf(3, 2, 3, 2, 3), // 27
      listOf(2, 1, 2, 3, 2), // 28
      listOf(2, 3, 2, 1, 2), // 29
      listOf(0, 2, 2, 2, 0), // 30
      listOf(4, 2, 2, 2, 4), // 31
      listOf(1, 3, 3, 3, 1), // 32
      listOf(3, 1, 1, 1, 3), // 33
      listOf(0, 0, 2, 4, 4), // 34
      listOf(4, 4, 2, 0, 0), // 35
      listOf(1, 1, 3, 1, 1), // 36
      listOf(3, 3, 1, 3, 3), // 37
      listOf(0, 3, 0, 3, 0), // 38
      listOf(4, 1, 4, 1, 4), // 39
      listOf(2, 0, 4, 0, 2), // 40
    ).mapIndexed { index, rows ->
      PaylineDefinition(
        number = index + 1,
        rows = rows,
        color = PAYLINE_PALETTE[index % PAYLINE_PALETTE.size],
      )
    }

  private val BASE_STRIP: List<SlotSymbol> =
    listOf(
      SlotSymbol.CARD_10,
      SlotSymbol.CARD_A,
      SlotSymbol.APOLLO,
      SlotSymbol.CARD_J,
      SlotSymbol.BUTKUS,
      SlotSymbol.CARD_Q,
      SlotSymbol.CLUBBER,
      SlotSymbol.CARD_K,
      SlotSymbol.WILD_ROCKY,
      SlotSymbol.CARD_10,
      SlotSymbol.DRAGO,
      SlotSymbol.CARD_J,
      SlotSymbol.BONUS_RB,
      SlotSymbol.CARD_A,
      SlotSymbol.CARD_Q,
      SlotSymbol.APOLLO,
      SlotSymbol.CARD_K,
      SlotSymbol.BUTKUS,
      SlotSymbol.CARD_10,
      SlotSymbol.CLUBBER,
      SlotSymbol.CARD_J,
      SlotSymbol.WILD_ROCKY,
      SlotSymbol.CARD_Q,
      SlotSymbol.DRAGO,
      SlotSymbol.CARD_A,
      SlotSymbol.BONUS_RB,
      SlotSymbol.CARD_K,
      SlotSymbol.BUTKUS,
      SlotSymbol.CARD_10,
      SlotSymbol.APOLLO,
    )

  val BASE_REEL_STRIPS: List<List<SlotSymbol>> =
    List(GRID_COLS) { col -> BASE_STRIP.drop(col * 3) + BASE_STRIP.take(col * 3) }

  val FREE_SPINS_REEL_STRIPS: List<List<SlotSymbol>> =
    BASE_REEL_STRIPS.map { strip ->
      strip + listOf(SlotSymbol.WILD_ROCKY, SlotSymbol.APOLLO, SlotSymbol.WILD_ROCKY, SlotSymbol.CLUBBER)
    }

  fun spinGrid(
    isFreeSpins: Boolean,
    stickyWildPositions: Set<Pair<Int, Int>> = emptySet(),
    random: Random = Random.Default,
  ): List<List<SlotSymbol>> {
    val strips = if (isFreeSpins) FREE_SPINS_REEL_STRIPS else BASE_REEL_STRIPS
    return strips.mapIndexed { col, strip ->
      val start = random.nextInt(strip.size)
      List(GRID_ROWS) { row ->
        if ((col to row) in stickyWildPositions) SlotSymbol.WILD_ROCKY
        else strip[(start + row) % strip.size]
      }
    }
  }

  fun evaluateSpin(
    grid: List<List<SlotSymbol>>,
    totalBetCents: Long,
    isFreeSpins: Boolean,
  ): SpinEvaluation {
    require(grid.size == GRID_COLS && grid.all { it.size == GRID_ROWS }) {
      "Grid must be 5 reels x 5 rows"
    }
    val lineUnitCents = (totalBetCents / TOTAL_PAYLINES.toLong()).coerceAtLeast(1L)
    val multiplier = if (isFreeSpins) 2 else 1

    val winningLines = mutableListOf<WinningLine>()
    for (payline in PAYLINES) {
      val lineSymbols = (0 until GRID_COLS).map { reel -> grid[reel][payline.rows[reel]] }
      val win = evaluateSinglePayline(payline, lineSymbols, lineUnitCents, multiplier)
      if (win != null) {
        winningLines.add(win)
      }
    }

    // RB Bonus Scatter anywhere on the 5x5 grid
    val rbPositions = mutableListOf<Pair<Int, Int>>()
    for (col in 0 until GRID_COLS) {
      for (row in 0 until GRID_ROWS) {
        if (grid[col][row].isBonusRb) {
          rbPositions.add(col to row)
        }
      }
    }
    val rbCount = rbPositions.size.coerceAtMost(5)
    val rbMult = SlotSymbol.BONUS_RB.payoutsByCount[rbCount] ?: 0
    val rbScatterPayoutCents = rbMult.toLong() * totalBetCents * multiplier
    val bonusTriggered = !isFreeSpins && rbPositions.size >= 3

    val totalWinCents = winningLines.sumOf { it.payoutCents } + rbScatterPayoutCents

    return SpinEvaluation(
      grid = grid,
      winningLines = winningLines,
      rbCount = rbPositions.size,
      rbPositions = rbPositions,
      rbScatterPayoutCents = rbScatterPayoutCents,
      bonusTriggered = bonusTriggered,
      multiplierApplied = multiplier,
      totalWinCents = totalWinCents,
    )
  }

  private fun evaluateSinglePayline(
    payline: PaylineDefinition,
    lineSymbols: List<SlotSymbol>,
    lineUnitCents: Long,
    multiplier: Int,
  ): WinningLine? {
    var pureWildCount = 0
    for (sym in lineSymbols) {
      if (sym.isWild) pureWildCount++ else break
    }
    val pureWildPay =
      (SlotSymbol.WILD_ROCKY.payoutsByCount[pureWildCount] ?: 0).toLong() * lineUnitCents * multiplier

    val targetRegular = lineSymbols.firstOrNull { !it.isWild }
    var regCount = 0
    var regPay = 0L
    if (targetRegular != null && targetRegular.isRegularPaySymbol) {
      for (sym in lineSymbols) {
        if (sym == targetRegular || sym.isWild) regCount++ else break
      }
      regPay = (targetRegular.payoutsByCount[regCount] ?: 0).toLong() * lineUnitCents * multiplier
    }

    return when {
      pureWildPay == 0L && regPay == 0L -> null
      pureWildPay >= regPay ->
        WinningLine(
          lineNumber = payline.number,
          symbol = SlotSymbol.WILD_ROCKY,
          matchCount = pureWildCount,
          positions = (0 until pureWildCount).map { reel -> reel to payline.rows[reel] },
          payoutCents = pureWildPay,
        )
      else ->
        WinningLine(
          lineNumber = payline.number,
          symbol = targetRegular!!,
          matchCount = regCount,
          positions = (0 until regCount).map { reel -> reel to payline.rows[reel] },
          payoutCents = regPay,
        )
    }
  }
}

object KnockoutMatchEngine {
  const val MAX_ROUNDS = 10
  const val ROUND_WIN_MULTIPLIER = 3L

  fun playNextRound(
    opponent: Opponent,
    completedRounds: List<RoundOutcome>,
    triggerBetCents: Long,
    random: Random = Random.Default,
  ): List<RoundOutcome> {
    if (completedRounds.size >= MAX_ROUNDS || completedRounds.any { it.result == RoundResult.ROCKY_KNOCKOUT }) {
      return emptyList()
    }
    val roundNumber = completedRounds.size + 1
    val roundReward = triggerBetCents * ROUND_WIN_MULTIPLIER
    val roll = random.nextFloat()

    val koThreshold = if (roundNumber >= 4) opponent.koProbability else opponent.koProbability * 0.45f
    return when {
      roll < koThreshold -> {
        val outcomes = mutableListOf<RoundOutcome>()
        outcomes.add(
          RoundOutcome(
            roundNumber = roundNumber,
            result = RoundResult.ROCKY_KNOCKOUT,
            rewardCents = roundReward,
            commentary = "DEVASTATING LEFT HOOK! ${opponent.displayName.uppercase()} IS DOWN!",
          )
        )
        for (rem in (roundNumber + 1)..MAX_ROUNDS) {
          outcomes.add(
            RoundOutcome(
              roundNumber = rem,
              result = RoundResult.ROCKY_KNOCKOUT,
              rewardCents = roundReward,
              commentary = "KO BONUS AWARDED FOR ROUND $rem!",
            )
          )
        }
        outcomes
      }
      roll < koThreshold + opponent.winRoundProbability ->
        listOf(
          RoundOutcome(
            roundNumber = roundNumber,
            result = RoundResult.ROCKY_WIN,
            rewardCents = roundReward,
            commentary = "Rocky wins Round $roundNumber on the judges' scorecards!",
          )
        )
      else ->
        listOf(
          RoundOutcome(
            roundNumber = roundNumber,
            result = RoundResult.OPPONENT_WIN,
            rewardCents = 0L,
            commentary = "${opponent.displayName} takes Round $roundNumber.",
          )
        )
    }
  }
}

object GambleEngine {
  private val RANKS = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

  fun drawCard(random: Random = Random.Default): GambleCard {
    val rank = RANKS[random.nextInt(RANKS.size)]
    val suit = CardSuit.entries[random.nextInt(CardSuit.entries.size)]
    return GambleCard(rank = rank, suit = suit)
  }

  fun isGuessCorrect(guess: CardColor, drawnCard: GambleCard): Boolean = drawnCard.color == guess
}

fun formatCredits(cents: Long): String {
  val negative = cents < 0
  val absCents = kotlin.math.abs(cents)
  val whole = absCents / 100
  val rem = absCents % 100
  val wholeFormatted = "%,d".format(whole)
  val sign = if (negative) "-" else ""
  return "$sign$wholeFormatted.${"%02d".format(rem)}"
}
