package com.capstone.nik.mixology

import android.app.Activity
import com.capstone.nik.mixology.data.AppleAuth
import com.capstone.nik.mixology.data.AppleAuthResult

class FakeAppleAuth : AppleAuth {
    var result: AppleAuthResult = AppleAuthResult.Success("apple-firebase-token", "Ada Lovelace")
    var signIns = 0

    override suspend fun signIn(activity: Activity): AppleAuthResult {
        signIns += 1
        return result
    }
}
