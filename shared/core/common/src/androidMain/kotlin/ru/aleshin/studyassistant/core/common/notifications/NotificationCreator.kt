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
package ru.aleshin.studyassistant.core.common.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.aleshin.studyassistant.core.common.extensions.generateRandomNumber
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationChronometer
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationDefaults
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationImportance
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationProgress
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationStyles
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationVisibility

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface NotificationCreator {

    fun createNotify(
        channelId: String,
        title: String,
        text: String,
        timeStamp: Long? = System.currentTimeMillis(),
        smallIcon: Int,
        largeIcon: Bitmap? = null,
        visibility: NotificationVisibility = NotificationVisibility.PUBLIC,
        priority: NotificationPriority = NotificationPriority.DEFAULT,
        actions: List<NotificationCompat.Action> = emptyList(),
        contentIntent: PendingIntent? = null,
        category: NotificationCategory? = null,
        notificationDefaults: NotificationDefaults = NotificationDefaults(),
        autoCancel: Boolean = true,
        silent: Boolean = false,
        ongoing: Boolean = false,
        chronometer: NotificationChronometer? = null,
        timeoutAfterMillis: Long? = null,
        style: NotificationStyles? = null,
        color: Int? = null,
        progress: NotificationProgress? = null,
    ): Notification

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotifyChannel(
        channelId: String,
        channelName: String,
        importance: NotificationImportance,
        defaults: NotificationDefaults,
    )

    fun showNotify(
        notification: Notification,
        notifyId: Int = generateRandomNumber(),
        tag: String? = null
    )

    fun cancelNotify(notifyId: Int, tag: String? = null)

    class Base constructor(
        private val context: Context,
    ) : NotificationCreator {

        private val notificationManager: NotificationManager
            get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        private val notificationManagerCompat: NotificationManagerCompat
            get() = NotificationManagerCompat.from(context)

        override fun createNotify(
            channelId: String,
            title: String,
            text: String,
            timeStamp: Long?,
            smallIcon: Int,
            largeIcon: Bitmap?,
            visibility: NotificationVisibility,
            priority: NotificationPriority,
            actions: List<NotificationCompat.Action>,
            contentIntent: PendingIntent?,
            category: NotificationCategory?,
            notificationDefaults: NotificationDefaults,
            autoCancel: Boolean,
            silent: Boolean,
            ongoing: Boolean,
            chronometer: NotificationChronometer?,
            timeoutAfterMillis: Long?,
            style: NotificationStyles?,
            color: Int?,
            progress: NotificationProgress?,
        ): Notification {
            val builder = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                true -> NotificationCompat.Builder(context, channelId)
                false -> NotificationCompat.Builder(context)
            }
            builder.apply {
                setContentTitle(title)
                setContentText(text)
                setVisibility(visibility.visibility)
                setPriority(priority.priority)
                if (timeStamp == null) setShowWhen(false) else setWhen(timeStamp)
                if (color != null) setColor(color)
                setSmallIcon(smallIcon)
                if (largeIcon != null) setLargeIcon(largeIcon)
                if (contentIntent != null) setContentIntent(contentIntent)
                setAutoCancel(autoCancel && !ongoing)
                setOngoing(ongoing)
                if (category != null) setCategory(category.category)
                if (silent) {
                    setSilent(true)
                    setOnlyAlertOnce(true)
                } else {
                    var defaults = 0
                    if (notificationDefaults.isVibrate) defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
                    if (notificationDefaults.isSound) defaults = defaults or NotificationCompat.DEFAULT_SOUND
                    if (notificationDefaults.isLights) defaults = defaults or NotificationCompat.DEFAULT_LIGHTS
                    if (defaults != 0) setDefaults(defaults)
                }
                chronometer?.let { value ->
                    setWhen(value.whenMillis)
                    setShowWhen(true)
                    setUsesChronometer(true)
                    if (value.countDown && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setChronometerCountDown(true)
                    }
                }
                timeoutAfterMillis?.let { timeout ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setTimeoutAfter(timeout.coerceAtLeast(0L))
                    }
                }
                if (progress != null) with(progress) { setProgress(max, value, isIndeterminate) }
                if (style != null) setStyle(style.style)
                actions.forEach { addAction(it) }
            }
            return builder.build()
        }

        override fun showNotify(notification: Notification, notifyId: Int, tag: String?) {
            if (!notificationManagerCompat.areNotificationsEnabled()) return
            try {
                notificationManagerCompat.notify(tag, notifyId, notification)
            } catch (exception: SecurityException) {
                Log.e(Constants.App.LOGGER_TAG, "Notification permission is not granted", exception)
            }
        }

        override fun cancelNotify(notifyId: Int, tag: String?) {
            notificationManagerCompat.cancel(tag, notifyId)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun createNotifyChannel(
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
}
