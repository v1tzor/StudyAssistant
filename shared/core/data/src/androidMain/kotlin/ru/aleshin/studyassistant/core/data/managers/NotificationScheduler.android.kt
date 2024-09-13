/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.data.managers

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.messages.LocalNotificationReceiver

/**
 * @author Stanislav Aleshin on 20.08.2024.
 */
actual class NotificationScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager,
) {

    actual fun scheduleNotification(
        id: Int,
        title: String,
        body: String,
        time: Instant
    ) {
        val intent = LocalNotificationReceiver.createIntent(context, title, body)
        val flag = FLAG_CANCEL_CURRENT or FLAG_MUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, flag)
        val timeMillis = time.toEpochMilliseconds()
        alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, timeMillis, pendingIntent)
    }

    actual fun scheduleRepeatNotification(
        id: Int,
        title: String,
        body: String,
        time: Instant,
        interval: Long
    ) {
        val intent = LocalNotificationReceiver.createIntent(context, title, body)
        val flag = FLAG_CANCEL_CURRENT or FLAG_MUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, flag)
        val timeMillis = time.toEpochMilliseconds()
        alarmManager.setInexactRepeating(RTC_WAKEUP, timeMillis, interval, pendingIntent)
    }

    actual fun cancelNotification(id: Int) {
        val intent = LocalNotificationReceiver.createCancelIntent(context)
        val cancelFlag = FLAG_CANCEL_CURRENT or FLAG_MUTABLE
        val cancelPendingIntent = PendingIntent.getBroadcast(context, id, intent, cancelFlag)
        alarmManager.cancel(cancelPendingIntent)
        cancelPendingIntent.cancel()
    }
}