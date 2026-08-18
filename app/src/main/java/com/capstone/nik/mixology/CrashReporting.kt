package com.capstone.nik.mixology

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal fun recordCrash(error: Throwable, log: String? = null) {
    runCatching {
        val crashlytics = FirebaseCrashlytics.getInstance()
        if (log != null) crashlytics.log(log)
        crashlytics.recordException(error)
    }
}
