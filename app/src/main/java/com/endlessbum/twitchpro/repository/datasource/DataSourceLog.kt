package com.endlessbum.twitchpro.repository.datasource

import android.util.Log
import com.endlessbum.twitchpro.BuildConfig

/**
 * One tag for the exceptions swallowed by the fallback chains (GQL -> persisted query -> Helix):
 * in debug builds each fallback is logged, so a silent "empty page" can be traced back to its cause.
 */
object DataSourceLog {
    private const val TAG = "XtraDataSource"

    fun log(e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "load fallback", e)
        }
    }
}
