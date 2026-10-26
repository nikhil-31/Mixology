package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ApiDetail
import com.google.gson.Gson

object ApiErrors {
    private val gson = Gson()

    fun detail(body: String?, fallback: String): String {
        val parsed = body?.let { raw ->
            runCatching { gson.fromJson(raw, ApiDetail::class.java)?.detail }.getOrNull()
        }
        return parsed?.takeIf { it.isNotBlank() } ?: fallback
    }
}
