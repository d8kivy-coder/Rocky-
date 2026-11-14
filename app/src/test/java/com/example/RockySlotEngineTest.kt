package com.example

import com.example.slot.engine.GambleEngine
import com.example.slot.engine.KnockoutMatchEngine
import com.example.slot.engine.RockySlotEngine
import com.example.slot.model.CardColor
import com.example.slot.model.CardSuit
import com.example.slot.model.GambleCard
import com.example.slot.model.Opponent
import com.example.slot.model.RoundResult
import com.example.slot.model.SlotSymbol
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RockySlotEngineTest {

  @Test
  fun `5x5 grid evaluates 5 wilds on center payline 1`() {
    val grid =
      List(5) {
        listOf(
          SlotSymbol.CARD_10,
          SlotSymbol.CARD_J,
          SlotSymbol.WILD_ROCKY,
          SlotSymbol.CARD_Q,
          SlotSymbol.CARD_K,
        )
      }

    val eval =
      RockySlotEngine.evaluateSpin(
        grid = grid,
        totalBetCents = 400L, // coin value = 10 cents across 40 lines
        isFreeSpins = false,
      )

    val line1 = eval.winningLines.first { it.lineNumber == 1 }
    assertEquals(SlotSymbol.WILD_ROCKY, line1.symbol)
    assertEquals(5, line1.matchCount)
    assertEquals(10_000L, line1.payoutCents) // 1000x * 10 cents
  }

  @Test
  fun `wild substitutes for Apollo on 5x5 payline 1`() {
    val grid =
      listOf(
        listOf(SlotSymbol.CARD_10, SlotSymbol.CARD_J, SlotSymbol.WILD_ROCKY, SlotSymbol.CARD_Q, SlotSymbol.CARD_K),
        listOf(SlotSymbol.CARD_J, SlotSymbol.CARD_Q, SlotSymbol.WILD_ROCKY, SlotSymbol.CARD_K, SlotSymbol.CARD_A),
        listOf(SlotSymbol.CARD_Q, SlotSymbol.CARD_K, SlotSymbol.APOLLO, SlotSymbol.CARD_A, SlotSymbol.CARD_10),
        listOf(SlotSymbol.CARD_K, SlotSymbol.CARD_A, SlotSymbol.APOLLO, SlotSymbol.CARD_10, SlotSymbol.CARD_J),
        listOf(SlotSymbol.CARD_A, SlotSymbol.CARD_10, SlotSymbol.BUTKUS, SlotSymbol.CARD_J, SlotSymbol.CARD_Q),
      )

    val eval = RockySlotEngine.evaluateSpin(grid = grid, totalBetCents = 400L, isFreeSpins = false)
    val line1 = eval.winningLines.first { it.lineNumber == 1 }
    assertEquals(SlotSymbol.APOLLO, line1.symbol)
    assertEquals(4, line1.matchCount)
    assertEquals(1_200L, line1.payoutCents) // 120x * 10 cents
  }

  @Test
  fun `3 RB bonus symbols trigger Movie Bonus Hub and scatter payout`() {
    val grid =
      listOf(
        listOf(SlotSymbol.BONUS_RB, SlotSymbol.CARD_J, SlotSymbol.CARD_10, SlotSymbol.CARD_Q, SlotSymbol.CARD_K),
        listOf(SlotSymbol.CARD_J, SlotSymbol.CARD_Q, SlotSymbol.CARD_K, SlotSymbol.CARD_A, SlotSymbol.CARD_10),
        listOf(SlotSymbol.CARD_Q, SlotSymbol.CARD_K, SlotSymbol.BONUS_RB, SlotSymbol.CARD_10, SlotSymbol.CARD_J),
        listOf(SlotSymbol.CARD_K, SlotSymbol.CARD_A, SlotSymbol.CARD_10, SlotSymbol.CARD_J, SlotSymbol.CARD_Q),
        listOf(SlotSymbol.CARD_A, SlotSymbol.CARD_10, SlotSymbol.CARD_J, SlotSymbol.CARD_Q, SlotSymbol.BONUS_RB),
      )

    val baseEval = RockySlotEngine.evaluateSpin(grid = grid, totalBetCents = 500L, isFreeSpins = false)
    assertEquals(3, baseEval.rbCount)
    assertTrue(baseEval.bonusTriggered)
    assertEquals(1_500L, baseEval.rbScatterPayoutCents)

    val freeEval = RockySlotEngine.evaluateSpin(grid = grid, totalBetCents = 500L, isFreeSpins = true)
    assertFalse(freeEval.bonusTriggered)
    assertEquals(3_000L, freeEval.rbScatterPayoutCents) // 2x multiplier in Free Spins
  }

  @Test
  fun `knockout match awards all remaining rounds on knockout`() {
    val koRandom =
      object : Random() {
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextFloat(): Float = 0.0f
      }
    val outcomes =
      KnockoutMatchEngine.playNextRound(
        opponent = Opponent.APOLLO,
        completedRounds = emptyList(),
        triggerBetCents = 500L,
        random = koRandom,
      )
    assertEquals(10, outcomes.size)
    assertTrue(outcomes.all { it.result == RoundResult.ROCKY_KNOCKOUT })
    assertEquals(15_000L, outcomes.sumOf { it.rewardCents })
  }

  @Test
  fun `gamble color check matches red and black suits`() {
    assertTrue(GambleEngine.isGuessCorrect(CardColor.RED, GambleCard("A", CardSuit.HEARTS)))
    assertTrue(GambleEngine.isGuessCorrect(CardColor.RED, GambleCard("K", CardSuit.DIAMONDS)))
    assertFalse(GambleEngine.isGuessCorrect(CardColor.RED, GambleCard("Q", CardSuit.SPADES)))
    assertTrue(GambleEngine.isGuessCorrect(CardColor.BLACK, GambleCard("10", CardSuit.CLUBS)))
  }
}
