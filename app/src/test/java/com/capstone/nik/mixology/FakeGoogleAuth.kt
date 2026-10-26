package com.capstone.nik.mixology

import android.app.Activity
import com.capstone.nik.mixology.data.GoogleAuth
import com.capstone.nik.mixology.data.GoogleAuthResult

class FakeGoogleAuth : GoogleAuth {
    var result: GoogleAuthResult = GoogleAuthResult.Success("firebase-id-token")
    var signIns = 0
    var signedOut = false

    override suspend fun signIn(activity: Activity): GoogleAuthResult {
        signIns += 1
        return result
    }

    override fun signOut() {
        signedOut = true
    }
}
