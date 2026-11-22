package com.endlessbum.twitchpro.repository

import android.annotation.SuppressLint
import android.net.http.HttpEngine
import com.endlessbum.twitchpro.model.id.DeviceCodeResponse
import com.endlessbum.twitchpro.model.id.TokenResponse
import com.endlessbum.twitchpro.model.id.ValidationResponse
import com.endlessbum.twitchpro.util.C
import com.endlessbum.twitchpro.util.NetworkUtils
import com.endlessbum.twitchpro.util.NetworkUtils.executeAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.chromium.net.CronetEngine
import org.chromium.net.apihelpers.UploadDataProviders
import java.util.concurrent.ExecutorService

class AuthRepository(
    private val httpEngine: Lazy<HttpEngine?>,
    private val cronetEngine: Lazy<CronetEngine?>,
    private val cronetExecutor: Lazy<ExecutorService>,
    private val okHttpClient: Lazy<OkHttpClient>,
    private val json: Json,
) {

    /** An auth endpoint answered with an HTTP error status (429 rate limit, 400 bad code...). */
    class HttpException(val status: Int) : Exception("HTTP $status")

    /** Decodes only when the HTTP status says success: an error body decodes into an "empty" success otherwise. */
    private inline fun <reified T> decodeChecked(body: String, status: Int): T {
        if (status !in 200..299) {
            throw HttpException(status)
        }
        return json.decodeFromString<T>(body)
    }

    suspend fun validate(networkLibrary: String?, token: String): ValidationResponse = withContext(Dispatchers.IO) {
        val url = "https://id.twitch.tv/oauth2/validate"
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        addHeader("Authorization", token)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode != 401) {
                    decodeChecked<ValidationResponse>(response.body.decodeToString(), response.info.httpStatusCode)
                } else {
                    throw IllegalStateException("401")
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
                        addHeader("Authorization", token)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                if (response.info.httpStatusCode != 401) {
                    decodeChecked<ValidationResponse>(response.body.decodeToString(), response.info.httpStatusCode)
                } else {
                    throw IllegalStateException("401")
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    header("Authorization", token)
                }.build()).executeAsync().use { response ->
                    if (response.code != 401) {
                        decodeChecked<ValidationResponse>(response.body.string(), response.code)
                    } else {
                        throw IllegalStateException("401")
                    }
                }
            }
        }
    }

    suspend fun revoke(networkLibrary: String?, body: String) = withContext(Dispatchers.IO) {
        val url = "https://id.twitch.tv/oauth2/revoke"
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                suspendCancellableCoroutine<NetworkUtils.CronetResponse> { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    header("Content-Type", "application/x-www-form-urlencoded")
                    post(body.toRequestBody())
                }.build()).executeAsync()
            }
        }
    }

    suspend fun getDeviceCode(networkLibrary: String?, body: String): DeviceCodeResponse = withContext(Dispatchers.IO) {
        val url = "https://id.twitch.tv/oauth2/device"
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<DeviceCodeResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<DeviceCodeResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    header("Content-Type", "application/x-www-form-urlencoded")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    decodeChecked<DeviceCodeResponse>(response.body.string(), response.code)
                }
            }
        }
    }

    suspend fun getToken(networkLibrary: String?, body: String): TokenResponse = withContext(Dispatchers.IO) {
        val url = "https://id.twitch.tv/oauth2/token"
        when {
            networkLibrary == C.HTTP_ENGINE && httpEngine.value != null -> @SuppressLint("NewApi") {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.HttpEngineTimeout()
                    val request = httpEngine.value!!.newUrlRequestBuilder(
                        url,
                        cronetExecutor.value,
                        NetworkUtils.ByteArrayUrlCallback(continuation, timeout)
                    ).apply {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setUploadDataProvider(NetworkUtils.ByteArrayUploadProvider(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<TokenResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            networkLibrary == C.CRONET && cronetEngine.value != null -> {
                val response = suspendCancellableCoroutine { continuation ->
                    val timeout = NetworkUtils.CronetTimeout()
                    val request = cronetEngine.value!!.newUrlRequestBuilder(
                        url,
                        NetworkUtils.ByteArrayCronetCallback(continuation, timeout),
                        cronetExecutor.value
                    ).apply {
                        addHeader("Content-Type", "application/x-www-form-urlencoded")
                        setUploadDataProvider(UploadDataProviders.create(body.toByteArray()), cronetExecutor.value)
                    }.build()
                    timeout.start(request, continuation)
                    request.start()
                    continuation.invokeOnCancellation {
                        request.cancel()
                        timeout.stop()
                    }
                }
                decodeChecked<TokenResponse>(response.body.decodeToString(), response.info.httpStatusCode)
            }
            else -> {
                okHttpClient.value.newCall(Request.Builder().apply {
                    url(url)
                    header("Content-Type", "application/x-www-form-urlencoded")
                    post(body.toRequestBody())
                }.build()).executeAsync().use { response ->
                    decodeChecked<TokenResponse>(response.body.string(), response.code)
                }
            }
        }
    }
}
