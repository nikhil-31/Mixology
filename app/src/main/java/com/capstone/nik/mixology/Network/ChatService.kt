package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ChatHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChatService {
    /** The newest [limit] messages, or those before message id [before]. Null params are omitted. */
    @GET("chat")
    suspend fun history(
        @Query("limit") limit: Int? = null,
        @Query("before") before: Long? = null,
    ): ChatHistoryResponse
}
