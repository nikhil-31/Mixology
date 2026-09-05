package com.capstone.nik.mixology.play

import android.app.Activity
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GooglePlayReviewClient @Inject constructor(
    private val reviewManager: ReviewManager,
) : PlayReviewClient {
    override suspend fun launch(activity: Activity): Boolean {
        val reviewInfo = reviewManager.requestReview()
        reviewManager.launchReview(activity, reviewInfo)
        return true
    }
}
