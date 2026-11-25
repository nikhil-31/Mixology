package com.capstone.nik.mixology

import kotlinx.coroutines.CancellationException

/**
 * [runCatching] for suspend work. A cancelled coroutine stays cancelled, where plain
 * `runCatching` would hand the cancellation back as a failure and let the code carry on.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Throwable) {
    Result.failure(error)
}
