package com.github.andreyasadchy.xtra.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import com.github.andreyasadchy.xtra.XtraApp
import com.github.andreyasadchy.xtra.ui.player.ExoPlayerService
import com.github.andreyasadchy.xtra.ui.player.MediaPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaButtonReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action == Intent.ACTION_MEDIA_BUTTON) {
            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.repeatCount == 0) {
                // the Room query goes to the IO thread under goAsync: runBlocking here ANRs the receiver
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val savedStates = (context.applicationContext as XtraApp).xtraModule.playerRepository.getPlaybackStates()
                        if (savedStates.isNotEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                when (context.prefs().getString(C.PLAYER, C.EXOPLAYER)) {
                                    C.MEDIA_PLAYER -> {
                                        context.startForegroundService(Intent(context, MediaPlayerService::class.java).apply {
                                            fillIn(intent, 0)
                                        })
                                    }
                                    else -> {
                                        context.startForegroundService(Intent(context, ExoPlayerService::class.java).apply {
                                            fillIn(intent, 0)
                                        })
                                    }
                                }
                            } else {
                                when (context.prefs().getString(C.PLAYER, C.EXOPLAYER)) {
                                    C.MEDIA_PLAYER -> {
                                        context.startService(Intent(context, MediaPlayerService::class.java).apply {
                                            fillIn(intent, 0)
                                        })
                                    }
                                    else -> {
                                        context.startService(Intent(context, ExoPlayerService::class.java).apply {
                                            fillIn(intent, 0)
                                        })
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // best effort: a failed media button press is silently dropped
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
