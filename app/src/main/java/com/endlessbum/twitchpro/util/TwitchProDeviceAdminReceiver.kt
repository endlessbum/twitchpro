package com.endlessbum.twitchpro.util

import android.app.admin.DeviceAdminReceiver

/**
 * Documented Android pattern: the manifest receiver must be a [DeviceAdminReceiver] subclass
 * (the platform class itself cannot be activated as device admin on modern APIs).
 * Used by the sleep timer to lock the screen.
 */
class TwitchProDeviceAdminReceiver : DeviceAdminReceiver()
