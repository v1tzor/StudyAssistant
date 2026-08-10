/*
 * Copyright 2026 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.studyassistant.application

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.TrafficStats
import android.os.Build
import android.os.StrictMode
import androidx.annotation.RequiresApi
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.supportKtorHttpUri
import ru.aleshin.studyassistant.android.R
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationDefaults
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationImportance

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
class StudyAssistantApp : BaseFlavorApplication(), SingletonSketch.Factory {

    private val notificationManager: NotificationManager
        get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun initSettings() {
        setupStrictMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotifyChannel(
                channelId = Constants.Notification.CHANNEL_ID,
                channelName = getString(R.string.notification_channel_common),
                importance = NotificationImportance.MAX,
                defaults = NotificationDefaults()
            )
            createNotifyChannel(
                channelId = Constants.Notification.ONGOING_CHANNEL_ID,
                channelName = getString(R.string.notification_channel_ongoing_classes),
                importance = NotificationImportance.LOW,
                defaults = NotificationDefaults(
                    isSound = false,
                    isVibrate = false,
                    isLights = false,
                ),
            )
        }
    }

    override fun createSketch(context: PlatformContext): Sketch {
        return Sketch.Builder(context).apply {
            addComponents {
                supportKtorHttpUri()
            }
        }.build()
    }

    private fun setupStrictMode() {
        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectCleartextNetwork()
            .detectFileUriExposure()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()

        val vmPolicy = when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.O..Build.VERSION_CODES.Q ->
                vmPolicyBuilder
                    .detectContentUriWithoutPermission()

            in Build.VERSION_CODES.Q..Build.VERSION_CODES.S ->
                vmPolicyBuilder
                    .detectContentUriWithoutPermission()
                    .detectCredentialProtectedWhileLocked()
                    .detectImplicitDirectBoot()

            in Build.VERSION_CODES.S..Int.MAX_VALUE ->
                vmPolicyBuilder
                    .detectContentUriWithoutPermission()
                    .detectCredentialProtectedWhileLocked()
                    .detectImplicitDirectBoot()
                    .detectIncorrectContextUse()
                    .detectUnsafeIntentLaunch()

            else -> vmPolicyBuilder
        }.build()

        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()

        StrictMode.setThreadPolicy(threadPolicy)
        StrictMode.setVmPolicy(vmPolicy)
        TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotifyChannel(
        channelId: String,
        channelName: String,
        importance: NotificationImportance,
        defaults: NotificationDefaults,
    ) {
        val channel = NotificationChannel(channelId, channelName, importance.importance).apply {
            enableLights(defaults.isLights)
            enableVibration(defaults.isVibrate)
            if (defaults.isVibrate) vibrationPattern = longArrayOf(500, 500, 500)
            if (!defaults.isSound) setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
