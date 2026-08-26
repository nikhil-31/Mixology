package com.capstone.nik.mixology.analytics

import android.content.Context
import android.os.Bundle
import com.capstone.nik.mixology.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class LoggedEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
)

fun analyticsScreenName(route: String?): String? {
    if (route.isNullOrBlank()) return null
    val path = route.substringBefore("?")
    return when {
        path.startsWith("details") -> SCREEN_DRINK_DETAILS
        path.startsWith("search") -> SCREEN_SEARCH
        path.startsWith("grid") -> SCREEN_DRINK_GRID
        path.startsWith("hot") -> SCREEN_HOME
        path.startsWith("randomixer") -> SCREEN_RANDOMIXER
        path.startsWith("settings") -> SCREEN_SETTINGS
        path.startsWith("catalog") -> SCREEN_CATALOG
        path.startsWith("bar") -> SCREEN_MY_BAR
        path.startsWith("shopping") -> SCREEN_SHOPPING_LIST
        else -> path.substringBefore("/").takeIf { it.isNotBlank() }
    }
}

const val SCREEN_HOME = "home"
const val SCREEN_DRINK_GRID = "drink_grid"
const val SCREEN_DRINK_DETAILS = "drink_details"
const val SCREEN_SEARCH = "search"
const val SCREEN_RANDOMIXER = "randomixer"
const val SCREEN_SETTINGS = "settings"
const val SCREEN_CATALOG = "catalog"
const val SCREEN_MY_BAR = "my_bar"
const val SCREEN_SHOPPING_LIST = "shopping_list"

const val EVENT_RANDOMIXER_SKIP = "randomixer_skip"
const val EVENT_REMOVE_FROM_WISHLIST = "remove_from_wishlist"
const val PARAM_SEARCH_MODE = "search_mode"
const val PARAM_RESULT_COUNT = "result_count"
const val PARAM_SAVED = "saved"

private const val MAX_PARAM_VALUE_LENGTH = 100
private const val ITEM_CATEGORY_DRINK = "drink"
private const val USER_PROPERTY_APP_VERSION = "app_version"

@Singleton
class AnalyticsTracker internal constructor(
    private val analytics: FirebaseAnalytics?,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(firebaseAnalyticsOrNull(context))

    internal val recorded = mutableListOf<LoggedEvent>()

    @Suppress("DEPRECATION")
    fun setCollectionEnabled(enabled: Boolean) {
        val instance = analytics ?: return
        runCatching {
            instance.setAnalyticsCollectionEnabled(enabled)
            if (enabled) {
                instance.setUserProperty(USER_PROPERTY_APP_VERSION, BuildConfig.VERSION_NAME)
                instance.setDefaultEventParameters(
                    Bundle().apply {
                        putString(USER_PROPERTY_APP_VERSION, BuildConfig.VERSION_NAME)
                    },
                )
            }
        }
    }

    fun logScreenView(screenName: String, screenClass: String = SCREEN_CLASS) {
        log(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to screenName,
                FirebaseAnalytics.Param.SCREEN_CLASS to screenClass,
            ),
        )
    }

    fun logSearch(query: String, mode: String, resultCount: Int) {
        log(
            FirebaseAnalytics.Event.SEARCH,
            mapOf(
                FirebaseAnalytics.Param.SEARCH_TERM to query,
                PARAM_SEARCH_MODE to mode,
                PARAM_RESULT_COUNT to resultCount,
            ),
        )
    }

    fun logViewDrink(id: String, name: String) {
        log(
            FirebaseAnalytics.Event.VIEW_ITEM,
            drinkParams(id, name),
        )
    }

    fun logSaveDrink(id: String, name: String, saved: Boolean) {
        log(
            if (saved) FirebaseAnalytics.Event.ADD_TO_WISHLIST else EVENT_REMOVE_FROM_WISHLIST,
            drinkParams(id, name) + (PARAM_SAVED to saved),
        )
    }

    fun logShareDrink(id: String, name: String) {
        log(
            FirebaseAnalytics.Event.SHARE,
            drinkParams(id, name) + (FirebaseAnalytics.Param.CONTENT_TYPE to ITEM_CATEGORY_DRINK),
        )
    }

    fun logAddToShoppingList(id: String, name: String) {
        log(
            FirebaseAnalytics.Event.ADD_TO_CART,
            drinkParams(id, name),
        )
    }

    fun logRandomixerSkip(id: String, name: String) {
        log(EVENT_RANDOMIXER_SKIP, drinkParams(id, name))
    }

    private fun drinkParams(id: String, name: String): Map<String, Any> = mapOf(
        FirebaseAnalytics.Param.ITEM_ID to id,
        FirebaseAnalytics.Param.ITEM_NAME to name,
        FirebaseAnalytics.Param.ITEM_CATEGORY to ITEM_CATEGORY_DRINK,
    )

    private fun log(name: String, params: Map<String, Any> = emptyMap()) {
        val instance = analytics
        if (instance == null) {
            recorded += LoggedEvent(name, params)
            return
        }
        runCatching {
            val bundle = Bundle()
            params.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value.take(MAX_PARAM_VALUE_LENGTH))
                    is Int -> bundle.putLong(key, value.toLong())
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Boolean -> bundle.putString(key, value.toString())
                }
            }
            instance.logEvent(name, bundle)
        }
    }

    companion object {
        const val SCREEN_CLASS = "ActivityMain"

        fun forTests(): AnalyticsTracker = AnalyticsTracker(null)

        private fun firebaseAnalyticsOrNull(context: Context): FirebaseAnalytics? {
            return try {
                FirebaseAnalytics.getInstance(context)
            } catch (_: Exception) {
                null
            }
        }
    }
}
