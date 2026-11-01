package com.capstone.nik.mixology.ui.shopping.history

import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.data.ShoppingTrip
import com.capstone.nik.mixology.repository.ShoppingTripRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** How a day of trips is headed. */
sealed interface TripDay {
    data object Today : TripDay
    data object Yesterday : TripDay
    /** Any other day; [startMillis] is that day's local midnight. */
    data class On(val startMillis: Long) : TripDay
}

data class TripDayGroup(val day: TripDay, val trips: List<ShoppingTrip>)

/** Trips (already newest first) under Today, Yesterday or their date, in the device's time zone. */
fun groupTripsByDay(trips: List<ShoppingTrip>, nowMillis: Long): List<TripDayGroup> {
    val today = startOfDay(nowMillis)
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today
        add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis
    return trips.groupBy { startOfDay(it.at) }.map { (start, dayTrips) ->
        val day = when (start) {
            today -> TripDay.Today
            yesterday -> TripDay.Yesterday
            else -> TripDay.On(start)
        }
        TripDayGroup(day, dayTrips)
    }
}

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

data class ShoppingHistoryUiState(
    val trips: List<ShoppingTrip> = emptyList(),
    val refreshing: Boolean = false,
    val loaded: Boolean = false,
) {
    fun trip(id: Long): ShoppingTrip? = trips.firstOrNull { it.id == id }
}

sealed interface ShoppingHistoryIntent {
    data class Delete(val id: Long) : ShoppingHistoryIntent
    data object Refresh : ShoppingHistoryIntent
}

/** Nothing one-off to show yet; deleting is immediate, as on iOS. */
sealed interface ShoppingHistoryEffect

@HiltViewModel
class ShoppingHistoryViewModel @Inject constructor(
    private val repository: ShoppingTripRepository,
) : MviViewModel<ShoppingHistoryIntent, ShoppingHistoryUiState, ShoppingHistoryEffect>(ShoppingHistoryUiState()) {

    init {
        viewModelScope.launch {
            repository.trips.collect { trips -> setState { copy(trips = trips, loaded = true) } }
        }
        viewModelScope.launch { repository.sync() }
    }

    override fun onIntent(intent: ShoppingHistoryIntent) {
        when (intent) {
            is ShoppingHistoryIntent.Delete -> viewModelScope.launch { repository.delete(intent.id) }
            ShoppingHistoryIntent.Refresh -> viewModelScope.launch {
                setState { copy(refreshing = true) }
                try {
                    repository.sync()
                } finally {
                    setState { copy(refreshing = false) }
                }
            }
        }
    }
}
