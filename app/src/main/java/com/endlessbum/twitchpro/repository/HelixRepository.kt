package com.github.andreyasadchy.xtra.repository

import android.annotation.SuppressLint
import android.net.http.HttpEngine
import androidx.core.net.toUri
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearchResponse
import com.github.andreyasadchy.xtra.model.helix.chat.BadgesResponse
import com.github.andreyasadchy.xtra.model.helix.chat.ChatUsersResponse
import com.github.andreyasadchy.xtra.model.helix.chat.CheerEmotesResponse
import com.github.andreyasadchy.xtra.model.helix.chat.EmoteSetsResponse
import com.github.andreyasadchy.xtra.model.helix.chat.UserEmotesResponse
import com.github.andreyasadchy.xtra.model.helix.clip.ClipsResponse
import com.github.andreyasadchy.xtra.model.helix.follow.FollowsResponse
import com.github.andreyasadchy.xtra.model.helix.follower.FollowersResponse
import com.github.andreyasadchy.xtra.model.helix.game.GamesResponse
import com.github.andreyasadchy.xtra.model.helix.stream.StreamsResponse
import com.github.andreyasadchy.xtra.model.helix.user.UsersResponse
import com.github.andreyasadchy.xtra.model.helix.video.VideosResponse
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.NetworkUtils
import com.github.andreyasadchy.xtra.util.NetworkUtils.executeAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.chromium.net.CronetEngine
import org.chromium.net.apihelpers.UploadDataProviders
import java.util.concurrent.ExecutorService

class HelixRepository(
    private val httpEngine: Lazy<HttpEngine?>,
    private val cronetEngine: Lazy<CronetEngine?>,
    private val cronetExecutor: Lazy<ExecutorService>,
    private val okHttpClient: Lazy<OkHttpClient>,
    private val json: Json,
) {

    /** A Helix endpoint answered with an HTTP error status (401 expired token, 403, 429 rate limit, 5xx...). */
    class HttpException(val status: Int) : Exception("HTTP $status")

    /** Decodes only when the HTTP status says success: an error body decodes into an "empty" success otherwise. */
    private inline fun <reified T> decodeChecked(body: String, status: Int): T {
        if (status !in 200..299) {
            throw HttpException(status)
        }
        return json.decodeFromString<T>(body)
    }

    suspend fun getGames(networkLibrary: String?, headers: Map<String, String>, ids: List<String>? = null, names: List<String>? = null): GamesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/games".toUri().buildUpon().apply {
            ids?.forEach { appendQueryParameter("id", it) }
            names?.forEach { appendQueryParameter("name", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<GamesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<GamesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<GamesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getTopGames(networkLibrary: String?, headers: Map<String, String>, limit: Int?, offset: String?): GamesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/games/top".toUri().buildUpon().apply {
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<GamesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<GamesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<GamesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getStreams(networkLibrary: String?, headers: Map<String, String>, ids: List<String>? = null, logins: List<String>? = null, gameId: String? = null, languages: List<String>? = null, limit: Int? = null, offset: String? = null): StreamsResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/streams".toUri().buildUpon().apply {
            ids?.forEach { appendQueryParameter("user_id", it) }
            logins?.forEach { appendQueryParameter("user_login", it) }
            gameId?.let { appendQueryParameter("game_id", it) }
            languages?.forEach { appendQueryParameter("language", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<StreamsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<StreamsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<StreamsResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getFollowedStreams(networkLibrary: String?, headers: Map<String, String>, userId: String?, limit: Int?, offset: String?): StreamsResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/streams/followed".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("user_id", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<StreamsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<StreamsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<StreamsResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getClips(networkLibrary: String?, headers: Map<String, String>, ids: List<String>? = null, channelId: String? = null, gameId: String? = null, startedAt: String? = null, endedAt: String? = null, limit: Int? = null, offset: String? = null): ClipsResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/clips".toUri().buildUpon().apply {
            ids?.forEach { appendQueryParameter("id", it) }
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            gameId?.let { appendQueryParameter("game_id", it) }
            startedAt?.let { appendQueryParameter("started_at", it) }
            endedAt?.let { appendQueryParameter("ended_at", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<ClipsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<ClipsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<ClipsResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getVideos(networkLibrary: String?, headers: Map<String, String>, ids: List<String>? = null, gameId: String? = null, channelId: String? = null, period: String? = null, broadcastType: String? = null, sort: String? = null, language: String? = null, limit: Int? = null, offset: String? = null): VideosResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/videos".toUri().buildUpon().apply {
            ids?.forEach { appendQueryParameter("id", it) }
            gameId?.let { appendQueryParameter("game_id", it) }
            channelId?.let { appendQueryParameter("user_id", it) }
            period?.let { appendQueryParameter("period", it) }
            broadcastType?.let { appendQueryParameter("type", it) }
            sort?.let { appendQueryParameter("sort", it) }
            language?.let { appendQueryParameter("language", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<VideosResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<VideosResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<VideosResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getUsers(networkLibrary: String?, headers: Map<String, String>, ids: List<String>? = null, logins: List<String>? = null): UsersResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/users".toUri().buildUpon().apply {
            ids?.forEach { appendQueryParameter("id", it) }
            logins?.forEach { appendQueryParameter("login", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<UsersResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<UsersResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<UsersResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getSearchGames(networkLibrary: String?, headers: Map<String, String>, query: String?, limit: Int?, offset: String?): GamesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/search/categories".toUri().buildUpon().apply {
            query?.let { appendQueryParameter("query", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<GamesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<GamesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<GamesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getSearchChannels(networkLibrary: String?, headers: Map<String, String>, query: String?, limit: Int?, offset: String?, live: Boolean? = null): ChannelSearchResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/search/channels".toUri().buildUpon().apply {
            query?.let { appendQueryParameter("query", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
            live?.let { appendQueryParameter("live_only", it.toString()) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<ChannelSearchResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<ChannelSearchResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<ChannelSearchResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getUserFollows(networkLibrary: String?, headers: Map<String, String>, userId: String?, targetId: String? = null, limit: Int? = null, offset: String? = null): FollowsResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/channels/followed".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("user_id", it) }
            targetId?.let { appendQueryParameter("broadcaster_id", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<FollowsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<FollowsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<FollowsResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getUserFollowers(networkLibrary: String?, headers: Map<String, String>, userId: String?, targetId: String? = null, limit: Int? = null, offset: String? = null): FollowersResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/channels/followers".toUri().buildUpon().apply {
            targetId?.let { appendQueryParameter("user_id", it) }
            userId?.let { appendQueryParameter("broadcaster_id", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<FollowersResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<FollowersResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<FollowersResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getUserEmotes(networkLibrary: String?, headers: Map<String, String>, userId: String?, channelId: String?, offset: String?): UserEmotesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/emotes/user".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("user_id", it) }
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<UserEmotesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<UserEmotesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<UserEmotesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getEmotesFromSet(networkLibrary: String?, headers: Map<String, String>, setIds: List<String>): EmoteSetsResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/emotes/set".toUri().buildUpon().apply {
            setIds.forEach { appendQueryParameter("emote_set_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<EmoteSetsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<EmoteSetsResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<EmoteSetsResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getGlobalBadges(networkLibrary: String?, headers: Map<String, String>): BadgesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/badges/global"
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<BadgesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<BadgesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<BadgesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getChannelBadges(networkLibrary: String?, headers: Map<String, String>, userId: String?): BadgesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/badges".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("broadcaster_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<BadgesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<BadgesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<BadgesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getCheerEmotes(networkLibrary: String?, headers: Map<String, String>, userId: String?): CheerEmotesResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/bits/cheermotes".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("broadcaster_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<CheerEmotesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<CheerEmotesResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<CheerEmotesResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getChatters(networkLibrary: String?, headers: Map<String, String>, channelId: String?, userId: String?, limit: Int? = null, offset: String? = null): ChatUsersResponse = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/chatters".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            userId?.let { appendQueryParameter("moderator_id", it) }
            limit?.let { appendQueryParameter("first", it.toString()) }
            offset?.let { appendQueryParameter("after", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<ChatUsersResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<ChatUsersResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    decodeChecked<ChatUsersResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun createEventSubSubscription(networkLibrary: String?, headers: Map<String, String>, userId: String?, channelId: String?, type: String?, sessionId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/eventsub/subscriptions"
        val body = buildJsonObject {
            put("type", type)
            put("version", "1")
            putJsonObject("condition") {
                put("broadcaster_user_id", channelId)
                put("user_id", userId)
            }
            putJsonObject("transport") {
                put("method", "websocket")
                put("session_id", sessionId)
            }
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    private fun parseSendMessageResponse(statusCode: Int, rawBody: String): String? {
        if (statusCode in 200..299) {
            val root = runCatching { org.json.JSONObject(rawBody) }.getOrNull() ?: return null
            val item = root.optJSONArray("data")?.optJSONObject(0) ?: return null
            if (!item.optBoolean("is_sent", true)) {
                val drop = item.optJSONObject("drop_reason")
                val code = drop?.optString("code").orEmpty()
                val msg = drop?.optString("message").orEmpty()
                return "DROP:$code|$msg"
            }
            return null
        }
        val msg = runCatching { org.json.JSONObject(rawBody).optString("message") }.getOrNull()?.takeIf { it.isNotBlank() }
        return msg ?: rawBody
    }

    suspend fun sendMessage(networkLibrary: String?, headers: Map<String, String>, userId: String?, channelId: String?, message: String?, replyId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/messages"
        val body = buildJsonObject {
            put("broadcaster_id", channelId)
            put("sender_id", userId)
            put("message", message)
            replyId?.let { put("reply_parent_message_id", it) }
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                parseSendMessageResponse(response.info.httpStatusCode, response.body.decodeToString())
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                parseSendMessageResponse(response.info.httpStatusCode, response.body.decodeToString())
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    parseSendMessageResponse(response.code, response.body.string())
                }
            }
        }
    }

    suspend fun sendAnnouncement(networkLibrary: String?, headers: Map<String, String>, channelId: String?, userId: String?, message: String?, color: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/announcements".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            userId?.let { appendQueryParameter("moderator_id", it) }
        }.build().toString()
        val body = buildJsonObject {
            put("message", message)
            color?.let { put("color", it) }
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun banUser(networkLibrary: String?, headers: Map<String, String>, channelId: String?, userId: String?, targetId: String?, duration: Int? = null, reason: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/moderation/bans".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            userId?.let { appendQueryParameter("moderator_id", it) }
        }.build().toString()
        val body = buildJsonObject {
            putJsonObject("data") {
                duration?.let { put("duration", it) }
                put("reason", reason)
                put("user_id", targetId)
            }
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun unbanUser(networkLibrary: String?, headers: Map<String, String>, channelId: String?, userId: String?, targetId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/moderation/bans".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            userId?.let { appendQueryParameter("moderator_id", it) }
            targetId?.let { appendQueryParameter("user_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    method("DELETE", null)
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun deleteMessages(networkLibrary: String?, headers: Map<String, String>, channelId: String?, userId: String?, messageId: String? = null): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/moderation/chat".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            userId?.let { appendQueryParameter("moderator_id", it) }
            messageId?.let { appendQueryParameter("message_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    method("DELETE", null)
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun getChatColor(networkLibrary: String?, headers: Map<String, String>, userId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/color".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("user_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    decodeChecked<JsonElement>(response.body.decodeToString(), response.info.httpStatusCode).jsonObject["data"]?.jsonArray?.firstOrNull()?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    decodeChecked<JsonElement>(response.body.decodeToString(), response.info.httpStatusCode).jsonObject["data"]?.jsonArray?.firstOrNull()?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        decodeChecked<JsonElement>(response.body.string(), response.code).jsonObject["data"]?.jsonArray?.firstOrNull()?.jsonObject?.get("color")?.jsonPrimitive?.contentOrNull
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun updateChatColor(networkLibrary: String?, headers: Map<String, String>, userId: String?, color: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/color".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("user_id", it) }
            color?.let { appendQueryParameter("color", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("PUT")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("PUT")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    method("PUT", null)
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun startCommercial(networkLibrary: String?, headers: Map<String, String>, channelId: String?, length: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/channels/commercial"
        val body = buildJsonObject {
            put("broadcaster_id", channelId)
            put("length", length?.toIntOrNull())
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        decodeChecked<JsonElement>(response.body.string(), response.code).jsonObject["data"]?.jsonArray?.firstOrNull()?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun updateChatSettings(networkLibrary: String?, headers: Map<String, String>, channelId: String?, userId: String?, emote: Boolean? = null, followers: Boolean? = null, followersDuration: Int? = null, slow: Boolean? = null, slowDuration: Int? = null, subs: Boolean? = null, unique: Boolean? = null): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/chat/settings".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            userId?.let { appendQueryParameter("moderator_id", it) }
        }.build().toString()
        val body = buildJsonObject {
            emote?.let { put("emote_mode", it) }
            followers?.let { put("follower_mode", it) }
            followersDuration?.let { put("follower_mode_duration", it) }
            slow?.let { put("slow_mode", it) }
            slowDuration?.let { put("slow_mode_wait_time", it) }
            subs?.let { put("subscriber_mode", it) }
            unique?.let { put("unique_chat_mode", it) }
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                        setHttpMethod("PATCH")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                        setHttpMethod("PATCH")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    method("PATCH", body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun createStreamMarker(networkLibrary: String?, headers: Map<String, String>, channelId: String?, description: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/streams/markers"
        val body = buildJsonObject {
            put("user_id", channelId)
            description?.let { put("description", it) }
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun addModerator(networkLibrary: String?, headers: Map<String, String>, channelId: String?, targetId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/moderation/moderators".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            targetId?.let { appendQueryParameter("user_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun removeModerator(networkLibrary: String?, headers: Map<String, String>, channelId: String?, targetId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/moderation/moderators".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            targetId?.let { appendQueryParameter("user_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    method("DELETE", null)
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun startRaid(networkLibrary: String?, headers: Map<String, String>, channelId: String?, targetId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/raids".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("from_broadcaster_id", it) }
            targetId?.let { appendQueryParameter("to_broadcaster_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun cancelRaid(networkLibrary: String?, headers: Map<String, String>, channelId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/raids".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    method("DELETE", null)
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun addVip(networkLibrary: String?, headers: Map<String, String>, channelId: String?, targetId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/channels/vips".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            targetId?.let { appendQueryParameter("user_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun removeVip(networkLibrary: String?, headers: Map<String, String>, channelId: String?, targetId: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/channels/vips".toUri().buildUpon().apply {
            channelId?.let { appendQueryParameter("broadcaster_id", it) }
            targetId?.let { appendQueryParameter("user_id", it) }
        }.build().toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        setHttpMethod("DELETE")
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    method("DELETE", null)
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }

    suspend fun sendWhisper(networkLibrary: String?, headers: Map<String, String>, userId: String?, targetId: String?, message: String?): String? = withContext(Dispatchers.IO) {
        val url = "https://api.twitch.tv/helix/whispers".toUri().buildUpon().apply {
            userId?.let { appendQueryParameter("from_user_id", it) }
            targetId?.let { appendQueryParameter("to_user_id", it) }
        }.build().toString()
        val body = buildJsonObject {
            put("message", message)
        }.toString()
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        headers.forEach { addHeader(it.key, it.value) }
                        addHeader("Content-Type", "application/json")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode in 200..299) {
                    null
                } else {
                    response.body.decodeToString()
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    headers(headers.toHeaders())
                    header("Content-Type", "application/json")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    if (response.isSuccessful) {
                        null
                    } else {
                        response.body.string()
                    }
                }
            }
        }
    }
}