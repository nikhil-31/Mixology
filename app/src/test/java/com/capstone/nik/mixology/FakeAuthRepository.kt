package com.capstone.nik.mixology

import com.capstone.nik.mixology.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var signedOut = false
    var deleted = false
    var deleteError: Exception? = null
    var export: String = """{"uid":"uid-1"}"""
    var exportError: Exception? = null

    override suspend fun signOut() {
        signedOut = true
    }

    override suspend fun deleteAccount() {
        deleteError?.let { throw it }
        deleted = true
    }

    override suspend fun exportData(): String {
        exportError?.let { throw it }
        return export
    }
}
