package com.example.slot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SlotSoundEngine
import com.example.data.PersistedSlotData
import com.example.data.SlotPreferencesRepository
import com.example.slot.engine.GambleEngine
import com.example.slot.engine.KnockoutMatchEngine
import com.example.slot.engine.RockySlotEngine
import com.example.slot.engine.formatCredits
import com.example.slot.model.CardColor
import com.example.slot.model.GambleState
import com.example.slot.model.KnockoutBonusState
import com.example.slot.model.MovieBonusHubState
import com.example.slot.model.Opponent
import com.example.slot.model.RockyMovieFeature
import com.example.slot.model.RoundResult
import com.example.slot.model.SlotStats
import com.example.slot.model.SlotSymbol
import com.example.slot.model.SpinEvaluation
import com.example.ui.components.WinCelebrationState
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RockySlotUiState(
  val balanceCents: Long = PersistedSlotData.DEFAULT_BALANCE_CENTS,
  val totalBetCents: Long = 500L,
  val grid: List<List<SlotSymbol>> = defaultInitial5x5Grid(),
  val reelSpinning: List<Boolean> = List(5) { false },
  val lastEvaluation: SpinEvaluation? = null,
  val highlightedLineIndex: Int = 0,
  val pendingWinCents: Long = 0L,
  val lastPaidWinCents: Long = 0L,
  val freeSpinsRemaining: Int = 0,
  val inFreeSpinsMode: Boolean = false,
  val freeSpinsTotalWinCents: Long = 0L,
  val stickyWildPositions: Set<Pair<Int, Int>> = emptySet(),
  val activeFeatureLabel: String? = null,
  val autoPlayRemaining: Int = 0,
  val isAutoPlayActive: Boolean = false,
  val soundEnabled: Boolean = true,
  val turboEnabled: Boolean = false,
  val statusMessage: String = "HIT SPIN TO START GAME",
  val movieBonusHub: MovieBonusHubState? = null,
  val knockoutBonus: KnockoutBonusState? = null,
  val gambleState: GambleState? = null,
  val showPaytable: Boolean = false,
  val showBetSelector: Boolean = false,
  val showAutoPlayDialog: Boolean = false,
  val winCelebration: WinCelebrationState? = null,
  val stats: SlotStats = SlotStats(),
) {
  val coinValueCents: Long
    get() = (totalBetCents / RockySlotEngine.TOTAL_PAYLINES.toLong()).coerceAtLeast(1L)

  val buyBonusCostCents: Long
    get() = totalBetCents * RockySlotEngine.BUY_BONUS_MULTIPLIER

  val isSpinning: Boolean
    get() = reelSpinning.any { it }

  val canGamble: Boolean
    get() =
      !isSpinning &&
        !inFreeSpinsMode &&
        !isAutoPlayActive &&
        pendingWinCents > 0L &&
        knockoutBonus == null &&
        movieBonusHub == null &&
        gambleState == null
}

// Mirrors the exact symbol distribution visible in the Playtech 5x5 Rocky screenshot
private fun defaultInitial5x5Grid(): List<List<SlotSymbol>> =
  listOf(
    listOf(SlotSymbol.CARD_10, SlotSymbol.APOLLO, SlotSymbol.CARD_J, SlotSymbol.CLUBBER, SlotSymbol.CARD_K),
    listOf(SlotSymbol.CARD_A, SlotSymbol.CARD_Q, SlotSymbol.CARD_A, SlotSymbol.WILD_ROCKY, SlotSymbol.BUTKUS),
    listOf(SlotSymbol.CARD_10, SlotSymbol.CARD_J, SlotSymbol.WILD_ROCKY, SlotSymbol.APOLLO, SlotSymbol.CARD_10),
    listOf(SlotSymbol.CARD_A, SlotSymbol.CARD_J, SlotSymbol.BUTKUS, SlotSymbol.BUTKUS, SlotSymbol.CARD_10),
    listOf(SlotSymbol.CARD_K, SlotSymbol.WILD_ROCKY, SlotSymbol.CLUBBER, SlotSymbol.APOLLO, SlotSymbol.CARD_J),
  )

class RockySlotViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = SlotPreferencesRepository(application.applicationContext)
  val soundEngine = SlotSoundEngine()

  private val _uiState = MutableStateFlow(RockySlotUiState())
  val uiState: StateFlow<RockySlotUiState> = _uiState.asStateFlow()

  private var spinJob: Job? = null
  private var lineCycleJob: Job? = null
  private var autoFightJob: Job? = null
  private var slamStopRequested = false

  init {
    viewModelScope.launch {
      val initial = repository.slotData.first()
      soundEngine.enabled = initial.soundEnabled
      _uiState.update {
        it.copy(
          balanceCents = initial.balanceCents,
          totalBetCents = initial.totalBetCents,
          soundEnabled = initial.soundEnabled,
          turboEnabled = initial.turboEnabled,
          stats = initial.stats,
        )
      }
      repository.slotData.collect { persisted ->
        _uiState.update { it.copy(stats = persisted.stats) }
      }
    }
  }

  fun onSpinOrStopClicked() {
    val state = _uiState.value
    if (state.isSpinning) {
      slamStopRequested = true
      return
    }
    if (state.knockoutBonus != null || state.gambleState != null || state.movieBonusHub != null) return
    startSpin()
  }

  fun startSpin() {
    val current = _uiState.value
    if (current.isSpinning) return

    var workingBalance = current.balanceCents
    if (current.pendingWinCents > 0L && !current.inFreeSpinsMode) {
      workingBalance += current.pendingWinCents
      _uiState.update { it.copy(balanceCents = workingBalance, pendingWinCents = 0L) }
      viewModelScope.launch { repository.saveBalance(workingBalance) }
    }

    val isFree = current.inFreeSpinsMode && current.freeSpinsRemaining > 0
    val wagerCents = current.totalBetCents

    if (!isFree && workingBalance < wagerCents) {
      _uiState.update {
        it.copy(
          isAutoPlayActive = false,
          autoPlayRemaining = 0,
          statusMessage = "LOW CREDITS — TAP BALANCE TO TOP UP",
        )
      }
      return
    }

    if (isFree) {
      val nextFree = (current.freeSpinsRemaining - 1).coerceAtLeast(0)
      _uiState.update {
        it.copy(
          freeSpinsRemaining = nextFree,
          statusMessage = "${current.activeFeatureLabel ?: "FREE GAMES"} • $nextFree LEFT",
        )
      }
    } else {
      workingBalance -= wagerCents
      _uiState.update {
        it.copy(
          balanceCents = workingBalance,
          lastPaidWinCents = 0L,
          pendingWinCents = 0L,
          statusMessage = "GOOD LUCK!",
        )
      }
      viewModelScope.launch { repository.saveBalance(workingBalance) }
    }

    lineCycleJob?.cancel()
    slamStopRequested = false

    val targetGrid =
      RockySlotEngine.spinGrid(
        isFreeSpins = isFree,
        stickyWildPositions = if (isFree) current.stickyWildPositions else emptySet(),
      )
    val turbo = _uiState.value.turboEnabled

    _uiState.update {
      it.copy(
        grid = targetGrid,
        reelSpinning = List(5) { true },
        lastEvaluation = null,
        highlightedLineIndex = 0,
      )
    }
    soundEngine.playSpinStart()

    spinJob?.cancel()
    spinJob =
      viewModelScope.launch {
        val initialDelay = if (turbo) 140L else 380L
        val stepDelay = if (turbo) 70L else 185L

        for (reel in 0 until 5) {
          val waitMs = if (reel == 0) initialDelay else stepDelay
          val chunks = (waitMs / 25L).coerceAtLeast(1L)
          for (c in 0 until chunks) {
            if (slamStopRequested) break
            delay(25L)
          }
          val stoppedList = _uiState.value.reelSpinning.toMutableList()
          stoppedList[reel] = false
          _uiState.update { it.copy(reelSpinning = stoppedList) }
          soundEngine.playReelStop(reel)
          if (slamStopRequested && reel < 4) delay(20L)
        }

        val evaluation =
          RockySlotEngine.evaluateSpin(
            grid = targetGrid,
            totalBetCents = wagerCents,
            isFreeSpins = isFree,
          )
        handleSpinEvaluation(evaluation, isFree, wagerCents)
      }
  }

  private fun handleSpinEvaluation(eval: SpinEvaluation, wasFreeSpin: Boolean, wagerCents: Long) {
    viewModelScope.launch {
      repository.recordSpinStats(
        winCents = eval.totalWinCents,
        hitBonus = eval.bonusTriggered,
        hitFreeGames = wasFreeSpin,
      )
    }

    val stateAfterReel = _uiState.value
    val isBigWin = eval.totalWinCents >= wagerCents * 10L && eval.totalWinCents > 0L

    if (eval.bonusTriggered) {
      soundEngine.playRockyFanfare()
    } else if (eval.totalWinCents > 0L) {
      soundEngine.playWinJingle(isBigWin)
    }

    // Collect any new Sticky Wilds if inside Free Spins
    val updatedStickies =
      if (wasFreeSpin) {
        val newWilds = mutableSetOf<Pair<Int, Int>>()
        newWilds.addAll(stateAfterReel.stickyWildPositions)
        for (col in 0 until 5) {
          for (row in 0 until 5) {
            if (eval.grid[col][row] == SlotSymbol.WILD_ROCKY && newWilds.size < 6) {
              newWilds.add(col to row)
            }
          }
        }
        newWilds
      } else {
        emptySet()
      }

    val creditImmediately = wasFreeSpin || eval.bonusTriggered
    val updatedBalance =
      if (creditImmediately && eval.totalWinCents > 0L) {
        stateAfterReel.balanceCents + eval.totalWinCents
      } else {
        stateAfterReel.balanceCents
      }

    if (creditImmediately && eval.totalWinCents > 0L) {
      viewModelScope.launch { repository.saveBalance(updatedBalance) }
    }

    val newFreeTotal =
      if (wasFreeSpin) stateAfterReel.freeSpinsTotalWinCents + eval.totalWinCents else 0L
    val remainingFree = stateAfterReel.freeSpinsRemaining
    val stayInFree = wasFreeSpin && remainingFree > 0

    val statusMsg = buildStatusMessage(eval, wasFreeSpin, remainingFree, newFreeTotal)
    val celebration =
      when {
        eval.bonusTriggered ->
          WinCelebrationState(
            title = "RB BONUS TRIGGERED!",
            subtitle = "${eval.rbCount}X RB SYMBOLS — PICK YOUR FEATURE",
            targetWinCents = eval.totalWinCents,
            isMega = true,
          )
        eval.totalWinCents >= wagerCents * 15L && eval.totalWinCents > 0L ->
          WinCelebrationState(
            title = "CHAMPIONSHIP WIN!",
            subtitle = "HEAVYWEIGHT KNOCKOUT PAYOUT",
            targetWinCents = eval.totalWinCents,
            isMega = true,
          )
        eval.totalWinCents >= wagerCents * 4L && eval.totalWinCents > 0L ->
          WinCelebrationState(
            title = "BIG WIN!",
            subtitle = "${eval.winningLines.size} WINNING LINES",
            targetWinCents = eval.totalWinCents,
            isMega = false,
          )
        else -> null
      }

    _uiState.update {
      it.copy(
        balanceCents = updatedBalance,
        lastEvaluation = eval,
        highlightedLineIndex = 0,
        pendingWinCents = if (!creditImmediately) eval.totalWinCents else 0L,
        lastPaidWinCents = eval.totalWinCents,
        inFreeSpinsMode = stayInFree,
        stickyWildPositions = if (stayInFree) updatedStickies else emptySet(),
        activeFeatureLabel = if (stayInFree) it.activeFeatureLabel else null,
        freeSpinsTotalWinCents = if (stayInFree) newFreeTotal else 0L,
        statusMessage = statusMsg,
        winCelebration = celebration,
        isAutoPlayActive = if (eval.bonusTriggered) false else it.isAutoPlayActive,
        autoPlayRemaining = if (eval.bonusTriggered) 0 else it.autoPlayRemaining,
        movieBonusHub =
          if (eval.bonusTriggered && celebration == null) {
            MovieBonusHubState(triggerBetCents = wagerCents, purchasedWithBuyBonus = false)
          } else null,
      )
    }

    if (eval.winningLines.isNotEmpty()) {
      startWinningLineCycling()
    }

    if (!eval.bonusTriggered) {
      if (stayInFree) {
        viewModelScope.launch {
          delay(if (_uiState.value.turboEnabled) 850L else 1550L)
          val latest = _uiState.value
          if (latest.inFreeSpinsMode && latest.freeSpinsRemaining > 0 && !latest.isSpinning && latest.movieBonusHub == null) {
            startSpin()
          }
        }
      } else if (_uiState.value.isAutoPlayActive) {
        scheduleNextAutoPlaySpin(hadWin = eval.totalWinCents > 0L)
      }
    }
  }

  private fun scheduleNextAutoPlaySpin(hadWin: Boolean) {
    viewModelScope.launch {
      val waitMs =
        when {
          _uiState.value.turboEnabled && hadWin -> 600L
          _uiState.value.turboEnabled -> 320L
          hadWin -> 1300L
          else -> 550L
        }
      delay(waitMs)
      val latest = _uiState.value
      if (
        latest.isAutoPlayActive &&
          !latest.isSpinning &&
          !latest.inFreeSpinsMode &&
          latest.knockoutBonus == null &&
          latest.movieBonusHub == null &&
          latest.gambleState == null
      ) {
        if (latest.autoPlayRemaining > 0) {
          val remainingAfterThis = latest.autoPlayRemaining - 1
          _uiState.update {
            it.copy(autoPlayRemaining = remainingAfterThis, isAutoPlayActive = remainingAfterThis > 0)
          }
          startSpin()
        } else {
          _uiState.update { it.copy(isAutoPlayActive = false) }
        }
      }
    }
  }

  private fun buildStatusMessage(
    eval: SpinEvaluation,
    wasFreeSpin: Boolean,
    remainingFree: Int,
    freeTotalWin: Long,
  ): String {
    if (eval.bonusTriggered) {
      return "${eval.rbCount}x RB BONUS! CHOOSE YOUR ROCKY FEATURE!"
    }
    if (wasFreeSpin && remainingFree == 0) {
      return "FREE GAMES COMPLETE! TOTAL WON: ${formatCredits(freeTotalWin)}"
    }
    if (eval.winningLines.isNotEmpty()) {
      val top = eval.winningLines.maxByOrNull { it.payoutCents }!!
      val lineCountStr = if (eval.winningLines.size > 1) "${eval.winningLines.size} LINES WIN" else "LINE ${top.lineNumber}"
      return "$lineCountStr • WIN ${formatCredits(eval.totalWinCents)}"
    }
    if (eval.rbScatterPayoutCents > 0L) {
      return "RB SCATTER PAYS ${formatCredits(eval.rbScatterPayoutCents)}!"
    }
    return if (wasFreeSpin) "FREE GAMES ($remainingFree LEFT)" else "HIT SPIN TO START GAME"
  }

  private fun startWinningLineCycling() {
    lineCycleJob?.cancel()
    lineCycleJob =
      viewModelScope.launch {
        while (true) {
          delay(1300L)
          _uiState.update { state ->
            val count = state.lastEvaluation?.winningLines?.size ?: 0
            if (count <= 1) state
            else state.copy(highlightedLineIndex = (state.highlightedLineIndex + 1) % count)
          }
        }
      }
  }

  fun dismissWinCelebration() {
    val current = _uiState.value
    val bonusPending = current.lastEvaluation?.bonusTriggered == true && current.movieBonusHub == null
    _uiState.update {
      it.copy(
        winCelebration = null,
        movieBonusHub =
          if (bonusPending) {
            MovieBonusHubState(triggerBetCents = current.totalBetCents, purchasedWithBuyBonus = false)
          } else it.movieBonusHub,
      )
    }
  }

  fun collectWin() {
    val current = _uiState.value
    if (current.pendingWinCents <= 0L) return
    soundEngine.playButtonClick()
    val newBalance = current.balanceCents + current.pendingWinCents
    _uiState.update {
      it.copy(
        balanceCents = newBalance,
        pendingWinCents = 0L,
        statusMessage = "COLLECTED ${formatCredits(current.pendingWinCents)}!",
      )
    }
    viewModelScope.launch { repository.saveBalance(newBalance) }
  }

  // --- Bet & Buy Bonus Controls ---
  fun openBetSelector(visible: Boolean) {
    if (_uiState.value.isSpinning || _uiState.value.inFreeSpinsMode) return
    soundEngine.playButtonClick()
    _uiState.update { it.copy(showBetSelector = visible) }
  }

  fun selectTotalBet(totalBetCents: Long) {
    if (_uiState.value.isSpinning || _uiState.value.inFreeSpinsMode) return
    soundEngine.playButtonClick()
    _uiState.update { it.copy(totalBetCents = totalBetCents, showBetSelector = false) }
    viewModelScope.launch { repository.saveTotalBet(totalBetCents) }
  }

  fun buyBonus() {
    val current = _uiState.value
    if (current.isSpinning || current.inFreeSpinsMode) return
    val cost = current.buyBonusCostCents
    val available = current.balanceCents + current.pendingWinCents
    if (available < cost) {
      _uiState.update { it.copy(statusMessage = "NEED ${formatCredits(cost)} FOR BUY BONUS — TAP BALANCE TO TOP UP") }
      return
    }
    soundEngine.playRockyFanfare()
    val newBalance = available - cost
    _uiState.update {
      it.copy(
        balanceCents = newBalance,
        pendingWinCents = 0L,
        isAutoPlayActive = false,
        autoPlayRemaining = 0,
        movieBonusHub =
          MovieBonusHubState(
            triggerBetCents = current.totalBetCents,
            purchasedWithBuyBonus = true,
          ),
      )
    }
    viewModelScope.launch { repository.saveBalance(newBalance) }
  }

  // --- Movie Bonus Hub (Rocky I - V + RB Training) ---
  fun selectMovieFeature(feature: RockyMovieFeature) {
    val hub = _uiState.value.movieBonusHub ?: return
    soundEngine.playBoxingBell()

    when (feature) {
      RockyMovieFeature.ROCKY_I,
      RockyMovieFeature.ROCKY_III -> {
        val spins = 8
        val initialStickies =
          if (feature == RockyMovieFeature.ROCKY_I) setOf(2 to 2) else emptySet()
        _uiState.update {
          it.copy(
            movieBonusHub = null,
            inFreeSpinsMode = true,
            freeSpinsRemaining = spins,
            freeSpinsTotalWinCents = 0L,
            stickyWildPositions = initialStickies,
            activeFeatureLabel = "${feature.numeral}: ${feature.title}",
            statusMessage = "${feature.title} — $spins FREE SPINS STARTING!",
          )
        }
        viewModelScope.launch {
          delay(650L)
          startSpin()
        }
      }
      RockyMovieFeature.ROCKY_IV -> {
        // Launch 10-round Knockout Showdown vs Drago/Clubber/Apollo!
        _uiState.update {
          it.copy(
            movieBonusHub = null,
            knockoutBonus = KnockoutBonusState(triggerBetCents = hub.triggerBetCents),
          )
        }
      }
      RockyMovieFeature.ROCKY_II,
      RockyMovieFeature.ROCKY_V,
      RockyMovieFeature.ROCKY_RB -> {
        _uiState.update {
          it.copy(movieBonusHub = hub.copy(pickedFeature = feature))
        }
      }
    }
  }

  fun pickBonusGlove(index: Int) {
    val hub = _uiState.value.movieBonusHub ?: return
    if (hub.pickedFeature != RockyMovieFeature.ROCKY_II || hub.glovePicksRemaining <= 0) return
    if (hub.revealedGloveMultipliers.getOrNull(index) != null) return

    val multPool = listOf(5, 8, 10, 15, 25, 40)
    val mult = multPool[Random.nextInt(multPool.size)]
    val updatedRevealed = hub.revealedGloveMultipliers.toMutableList()
    updatedRevealed[index] = mult

    val nextPicks = hub.glovePicksRemaining - 1
    val nextWin = hub.featureAwardCents + hub.triggerBetCents * mult.toLong()
    soundEngine.playPunchImpact(isKnockout = mult >= 25)

    _uiState.update {
      it.copy(
        movieBonusHub =
          hub.copy(
            glovePicksRemaining = nextPicks,
            revealedGloveMultipliers = updatedRevealed,
            featureAwardCents = nextWin,
            isResolved = nextPicks == 0,
          )
      )
    }
  }

  fun spinChampionshipWheel() {
    val hub = _uiState.value.movieBonusHub ?: return
    if (hub.pickedFeature != RockyMovieFeature.ROCKY_V || hub.wheelSpinning || hub.isResolved) return
    soundEngine.playSpinStart()
    _uiState.update { it.copy(movieBonusHub = hub.copy(wheelSpinning = true)) }

    viewModelScope.launch {
      delay(950L)
      val multipliers = listOf(10, 15, 20, 25, 40, 60, 100)
      val mult = multipliers[Random.nextInt(multipliers.size)]
      val latest = _uiState.value.movieBonusHub ?: return@launch
      val reward = latest.triggerBetCents * mult.toLong()
      soundEngine.playRockyFanfare()
      _uiState.update {
        it.copy(
          movieBonusHub =
            latest.copy(
              wheelSpinning = false,
              trainingStep = mult,
              featureAwardCents = reward,
              isResolved = true,
            )
        )
      }
    }
  }

  fun advanceTrainingSteps() {
    val hub = _uiState.value.movieBonusHub ?: return
    if (hub.pickedFeature != RockyMovieFeature.ROCKY_RB || hub.isResolved) return

    val stepGain = Random.nextInt(1, 4)
    val nextStep = (hub.trainingStep + stepGain).coerceAtMost(10)
    val multPerStep = listOf(0, 5, 10, 15, 20, 30, 40, 50, 65, 80, 100)[nextStep]
    val reward = hub.triggerBetCents * multPerStep.toLong()
    val finished = nextStep >= 10 || (nextStep >= 3 && Random.nextFloat() < 0.25f)
    soundEngine.playPunchImpact(isKnockout = finished)

    _uiState.update {
      it.copy(
        movieBonusHub =
          hub.copy(
            trainingStep = nextStep,
            featureAwardCents = reward,
            isResolved = finished,
          )
      )
    }
  }

  fun collectMovieBonusHub() {
    val hub = _uiState.value.movieBonusHub ?: return
    val reward = hub.featureAwardCents
    soundEngine.playWinJingle(isBigWin = true)
    val newBalance = _uiState.value.balanceCents + reward
    _uiState.update {
      it.copy(
        balanceCents = newBalance,
        lastPaidWinCents = reward,
        movieBonusHub = null,
        statusMessage = "BONUS COLLECTED: ${formatCredits(reward)}!",
      )
    }
    viewModelScope.launch {
      repository.saveBalance(newBalance)
      repository.updateBiggestWin(reward)
    }
  }

  // --- Autoplay & Toggles ---
  fun openAutoPlayDialog() {
    if (_uiState.value.inFreeSpinsMode) return
    soundEngine.playButtonClick()
    if (_uiState.value.isAutoPlayActive) {
      _uiState.update { it.copy(isAutoPlayActive = false, autoPlayRemaining = 0) }
    } else {
      _uiState.update { it.copy(showAutoPlayDialog = true) }
    }
  }

  fun dismissAutoPlayDialog() {
    _uiState.update { it.copy(showAutoPlayDialog = false) }
  }

  fun startAutoPlay(count: Int) {
    soundEngine.playButtonClick()
    _uiState.update {
      it.copy(
        showAutoPlayDialog = false,
        isAutoPlayActive = true,
        autoPlayRemaining = (count - 1).coerceAtLeast(0),
      )
    }
    if (!_uiState.value.isSpinning) startSpin()
  }

  fun toggleSound() {
    val next = !_uiState.value.soundEnabled
    soundEngine.enabled = next
    if (next) soundEngine.playButtonClick()
    _uiState.update { it.copy(soundEnabled = next) }
    viewModelScope.launch { repository.saveToggles(next, _uiState.value.turboEnabled) }
  }

  fun toggleTurbo() {
    val next = !_uiState.value.turboEnabled
    soundEngine.playButtonClick()
    _uiState.update { it.copy(turboEnabled = next) }
    viewModelScope.launch { repository.saveToggles(_uiState.value.soundEnabled, next) }
  }

  fun topUpFreeCredits() {
    soundEngine.playWinJingle(isBigWin = false)
    val nextBalance = _uiState.value.balanceCents + 500_000L
    _uiState.update {
      it.copy(
        balanceCents = nextBalance,
        statusMessage = "+5,000.00 FREE CREDITS ADDED!",
      )
    }
    viewModelScope.launch { repository.saveBalance(nextBalance) }
  }

  fun setPaytableVisible(visible: Boolean) {
    soundEngine.playButtonClick()
    _uiState.update { it.copy(showPaytable = visible) }
  }

  // --- Knockout Bonus Feature ---
  fun selectKnockoutOpponent(opponent: Opponent) {
    val bonus = _uiState.value.knockoutBonus ?: return
    if (bonus.selectedOpponent != null) return
    soundEngine.playBoxingBell()
    _uiState.update { it.copy(knockoutBonus = bonus.copy(selectedOpponent = opponent)) }
  }

  fun fightNextKnockoutRound() {
    val bonus = _uiState.value.knockoutBonus ?: return
    val opponent = bonus.selectedOpponent ?: return
    if (bonus.isComplete) return

    val newOutcomes =
      KnockoutMatchEngine.playNextRound(
        opponent = opponent,
        completedRounds = bonus.rounds,
        triggerBetCents = bonus.triggerBetCents,
      )
    if (newOutcomes.isEmpty()) return

    val isKo = newOutcomes.any { it.result == RoundResult.ROCKY_KNOCKOUT }
    soundEngine.playPunchImpact(isKnockout = isKo)

    val allRounds = bonus.rounds + newOutcomes
    val totalBonusWin = allRounds.sumOf { it.rewardCents }
    val finished = isKo || allRounds.size >= KnockoutMatchEngine.MAX_ROUNDS

    if (finished) {
      autoFightJob?.cancel()
      if (totalBonusWin > 0L) soundEngine.playRockyFanfare()
    }

    _uiState.update {
      it.copy(
        knockoutBonus =
          bonus.copy(
            rounds = allRounds,
            currentRound = allRounds.last().roundNumber,
            totalBonusWinCents = totalBonusWin,
            isComplete = finished,
            isAutoFighting = if (finished) false else bonus.isAutoFighting,
          )
      )
    }
  }

  fun toggleAutoFightKnockout() {
    val bonus = _uiState.value.knockoutBonus ?: return
    if (bonus.selectedOpponent == null || bonus.isComplete) return
    if (bonus.isAutoFighting) {
      autoFightJob?.cancel()
      _uiState.update { it.copy(knockoutBonus = bonus.copy(isAutoFighting = false)) }
      return
    }
    _uiState.update { it.copy(knockoutBonus = bonus.copy(isAutoFighting = true)) }
    autoFightJob =
      viewModelScope.launch {
        while (true) {
          val latest = _uiState.value.knockoutBonus ?: break
          if (latest.isComplete || !latest.isAutoFighting) break
          fightNextKnockoutRound()
          delay(520L)
        }
      }
  }

  fun collectKnockoutBonus() {
    val bonus = _uiState.value.knockoutBonus ?: return
    autoFightJob?.cancel()
    soundEngine.playWinJingle(isBigWin = true)
    val reward = bonus.totalBonusWinCents
    val newBalance = _uiState.value.balanceCents + reward
    _uiState.update {
      it.copy(
        balanceCents = newBalance,
        lastPaidWinCents = it.lastPaidWinCents + reward,
        knockoutBonus = null,
        statusMessage = "KNOCKOUT BONUS COLLECTED: ${formatCredits(reward)}!",
      )
    }
    viewModelScope.launch {
      repository.saveBalance(newBalance)
      repository.updateBiggestWin(reward)
    }
  }

  // --- Gamble (Double or Nothing) Feature ---
  fun openGamble() {
    val state = _uiState.value
    if (!state.canGamble) return
    soundEngine.playButtonClick()
    _uiState.update {
      it.copy(
        gambleState =
          GambleState(
            currentWinCents = state.pendingWinCents,
            history = List(4) { GambleEngine.drawCard() },
          )
      )
    }
  }

  fun gamblePickColor(guess: CardColor) {
    val gamble = _uiState.value.gambleState ?: return
    if (gamble.isFlipping || gamble.isFinished || gamble.currentWinCents <= 0L) return
    _uiState.update { it.copy(gambleState = gamble.copy(isFlipping = true)) }

    viewModelScope.launch {
      delay(400L)
      val card = GambleEngine.drawCard()
      val won = GambleEngine.isGuessCorrect(guess, card)
      soundEngine.playCardFlip(won)

      val latestGamble = _uiState.value.gambleState ?: return@launch
      val nextWin = if (won) latestGamble.currentWinCents * 2L else 0L
      val updatedHistory = (listOf(card) + latestGamble.history).take(6)

      _uiState.update {
        it.copy(
          gambleState =
            latestGamble.copy(
              currentWinCents = nextWin,
              history = updatedHistory,
              lastDrawnCard = card,
              lastGuessWon = won,
              isFlipping = false,
              isFinished = !won,
            )
        )
      }
    }
  }

  fun gambleHalf() {
    val gamble = _uiState.value.gambleState ?: return
    if (gamble.isFlipping || gamble.isFinished || gamble.currentWinCents < 2L) return
    soundEngine.playButtonClick()
    val halfToBank = gamble.currentWinCents / 2L
    val remaining = gamble.currentWinCents - halfToBank
    _uiState.update {
      it.copy(
        gambleState = gamble.copy(currentWinCents = remaining, bankedCents = gamble.bankedCents + halfToBank)
      )
    }
  }

  fun collectGamble() {
    val gamble = _uiState.value.gambleState ?: return
    if (gamble.isFlipping) return
    soundEngine.playButtonClick()
    val totalCollected = gamble.currentWinCents + gamble.bankedCents
    val newBalance = _uiState.value.balanceCents + totalCollected
    _uiState.update {
      it.copy(
        balanceCents = newBalance,
        pendingWinCents = 0L,
        lastPaidWinCents = totalCollected,
        gambleState = null,
        statusMessage =
          if (totalCollected > 0L) "GAMBLE COLLECTED: ${formatCredits(totalCollected)}!"
          else "HIT SPIN TO START GAME",
      )
    }
    viewModelScope.launch {
      repository.saveBalance(newBalance)
      if (totalCollected > 0L) repository.updateBiggestWin(totalCollected)
    }
  }

  override fun onCleared() {
    super.onCleared()
    spinJob?.cancel()
    lineCycleJob?.cancel()
    autoFightJob?.cancel()
  }
}
