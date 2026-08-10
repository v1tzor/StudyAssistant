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

package ru.aleshin.studyassistant.core.data.managers.reminders

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.os.Build
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
        val intent = LocalNotificationReceiver.createIntent(context, id, title, body)
        val flag = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, flag)
        scheduleAlarm(time.toEpochMilliseconds(), pendingIntent)
    }

    actual fun scheduleOngoingNotification(
        id: Int,
        title: String,
        body: String,
        time: Instant,
        endTime: Instant,
    ) {
        val intent = LocalNotificationReceiver.createOngoingIntent(
            context = context,
            id = id,
            title = title,
            body = body,
            endTime = endTime.toEpochMilliseconds(),
        )
        val flag = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, flag)
        scheduleAlarm(time.toEpochMilliseconds(), pendingIntent)
    }

    actual fun updateNotificationGroup(group: String, notificationIds: List<Int>) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val storedIds = preferences.getStringSet(group, emptySet()).orEmpty().mapNotNull(String::toIntOrNull)
        storedIds.filterNot(notificationIds::contains).forEach(::cancelNotification)
        preferences.edit().putStringSet(group, notificationIds.map(Int::toString).toSet()).apply()
    }

    actual fun clearNotificationGroup(group: String) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val storedIds = preferences.getStringSet(group, emptySet()).orEmpty().mapNotNull(String::toIntOrNull)
        storedIds.forEach(::cancelNotification)
        preferences.edit().remove(group).apply()
    }

    actual fun cancelNotification(id: Int) {
        val intent = LocalNotificationReceiver.createCancelIntent(context)
        val cancelFlag = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        val cancelPendingIntent = PendingIntent.getBroadcast(context, id, intent, cancelFlag)
        alarmManager.cancel(cancelPendingIntent)
        cancelPendingIntent.cancel()
        LocalNotificationReceiver.cancelNotification(context, id)
    }

    private fun scheduleAlarm(timeMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, timeMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(RTC_WAKEUP, timeMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            scheduleInexactAlarm(timeMillis, pendingIntent)
        } catch (_: RuntimeException) {
            scheduleInexactAlarm(timeMillis, pendingIntent)
        }
    }

    private fun scheduleInexactAlarm(timeMillis: Long, pendingIntent: PendingIntent) {
        runCatching {
            alarmManager.setAndAllowWhileIdle(RTC_WAKEUP, timeMillis, pendingIntent)
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "scheduled_notification_groups"
    }
}
