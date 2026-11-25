package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.RatingService
import com.capstone.nik.mixology.Network.RatingWrite
import com.capstone.nik.mixology.billing.SubscriptionRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/** A drink's 1–5 star ratings: everyone's average and the user's own. */
data class DrinkRating(
    val average: Double?,
    val count: Int,
    val mine: Int?,
)

interface RatingRepository {
    suspend fun rating(drinkId: String): DrinkRating

    /** Sets the user's rating, or clears it when [stars] is null. */
    suspend fun rate(drinkId: String, stars: Int?): DrinkRating

    companion object {
        /** For tests and previews: no ratings, and rating does nothing. */
        val None = object : RatingRepository {
            override suspend fun rating(drinkId: String) = DrinkRating(null, 0, null)
            override suspend fun rate(drinkId: String, stars: Int?) = DrinkRating(null, 0, stars)
        }
    }
}

@Singleton
class NetworkRatingRepository @Inject constructor(
    private val service: RatingService,
    private val subscriptions: SubscriptionRepository,
) : RatingRepository {

    override suspend fun rating(drinkId: String): DrinkRating = call {
        service.rating(drinkId).toRating()
    }

    override suspend fun rate(drinkId: String, stars: Int?): DrinkRating = call {
        val dto = if (stars == null) service.clear(drinkId) else service.rate(drinkId, RatingWrite(stars))
        dto.toRating()
    }

    private suspend fun call(block: suspend () -> DrinkRating): DrinkRating {
        try {
            return block()
        } catch (error: HttpException) {
            // Rating is part of Premium: a 403 means the trial has ended.
            if (error.code() == 403) subscriptions.requireSubscription()
            throw error
        }
    }

    private fun com.capstone.nik.mixology.Network.RatingDto.toRating() = DrinkRating(average, count, myRating)
}
