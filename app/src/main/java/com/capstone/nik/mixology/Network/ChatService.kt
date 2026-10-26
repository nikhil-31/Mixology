package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ChatHistoryResponse
import retrofit2.http.GET

interface ChatService {
    @GET("chat")
    suspend fun history(): ChatHistoryResponse
}
