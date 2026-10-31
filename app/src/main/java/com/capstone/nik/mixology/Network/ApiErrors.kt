package com.capstone.nik.mixology.Network

import android.content.Context
import androidx.annotation.StringRes
import com.capstone.nik.mixology.Network.remoteModel.ApiDetail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.repository.RecipeHttpException
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Why a request got no answer; anything else is the server's own error. */
enum class ConnectionError(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
) {
    /** The device has no network. */
    NO_CONNECTION(R.string.error_no_connection_title, R.string.error_no_connection_body),

    /** The network is up but the server refused, timed out or sent 503. */
    SERVER_UNREACHABLE(R.string.error_server_unreachable_title, R.string.error_server_unreachable_body),
}

object ApiErrors {
    private val gson = Gson()

    fun detail(body: String?, fallback: String): String {
        val parsed = body?.let { raw ->
            runCatching { gson.fromJson(raw, ApiDetail::class.java)?.detail }.getOrNull()
        }
        return parsed?.takeIf { it.isNotBlank() } ?: fallback
    }

    /**
     * The connection problem behind [error], or null when the server answered.
     * [online] is [NetworkMonitor.online], which tells the two kinds apart.
     */
    fun connectionError(error: Throwable, online: Boolean): ConnectionError? {
        val status = when (error) {
            is IOException -> null
            is HttpException -> error.code()
            is ChatHttpException -> error.code
            is RecipeHttpException -> error.code
            else -> return null
        }
        if (status != null && status != SERVICE_UNAVAILABLE) return null
        return if (online) ConnectionError.SERVER_UNREACHABLE else ConnectionError.NO_CONNECTION
    }

    private const val SERVICE_UNAVAILABLE = 503
}

/** [ApiErrors.connectionError] for the device's current network. */
fun NetworkMonitor.connectionError(error: Throwable): ConnectionError? =
    ApiErrors.connectionError(error, online.value)

/** The message for [error]: the connection problem behind it, or [fallback]. */
@StringRes
fun NetworkMonitor.errorMessageRes(error: Throwable, @StringRes fallback: Int): Int =
    connectionError(error)?.messageRes ?: fallback

/** [error]'s connection problem as a message to show, or null when the server answered. */
fun NetworkMonitor.connectionMessage(context: Context, error: Throwable): String? =
    connectionError(error)?.let { context.getString(it.messageRes) }
