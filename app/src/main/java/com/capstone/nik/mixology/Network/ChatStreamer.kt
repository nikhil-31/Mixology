package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ChatRequest
import com.capstone.nik.mixology.di.AuthenticatedClient
import com.google.gson.Gson
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatStreamer @Inject constructor(
    @AuthenticatedClient private val client: OkHttpClient,
) {
    private val gson = Gson()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val jpegType = "image/jpeg".toMediaType()

    fun stream(message: String, recipeAction: String? = null, recipeId: String? = null): Flow<ChatStreamEvent> {
        val request = Request.Builder()
            .url(CocktailURLs.BASE_URL + "chat")
            .header("Accept", "text/event-stream")
            .post(gson.toJson(ChatRequest(message, recipeAction, recipeId)).toRequestBody(jsonType))
            .build()
        return events(request)
    }

    /** A photo of recipe notes for Ada to read, with [message] as what the user said with it. */
    fun streamPhoto(jpeg: ByteArray, message: String): Flow<ChatStreamEvent> {
        val url = (CocktailURLs.BASE_URL + "chat/photo").toHttpUrl().newBuilder()
            .apply { if (message.isNotBlank()) addQueryParameter("message", message) }
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .post(jpeg.toRequestBody(jpegType))
            .build()
        return events(request)
    }

    private fun events(request: Request): Flow<ChatStreamEvent> = flow {
        val call = client.newCall(request)
        try {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    val detail = ApiErrors.detail(response.body?.string(), response.message)
                    throw ChatHttpException(response.code, detail)
                }
                val source = response.body?.source()
                    ?: throw ChatHttpException(response.code, response.message)
                val parser = SseParser()
                suspend fun emitEvents(events: List<SseEvent>): Boolean {
                    for (event in events) {
                        val parsed = event.toChatStreamEvent() ?: continue
                        emit(parsed)
                        if (parsed is ChatStreamEvent.Error) return true
                    }
                    return false
                }
                while (true) {
                    coroutineContext.ensureActive()
                    val line = source.readUtf8Line() ?: break
                    if (emitEvents(parser.push("$line\n"))) return@flow
                }
                emitEvents(parser.finish())
            }
        } finally {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)
}
