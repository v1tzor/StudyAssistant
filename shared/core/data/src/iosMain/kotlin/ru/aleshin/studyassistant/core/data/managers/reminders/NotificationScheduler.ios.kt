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

import kotlinx.datetime.Instant
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import ru.aleshin.studyassistant.core.common.managers.DateManager

/**
 * @author Stanislav Aleshin on 20.08.2024.
 */
actual class NotificationScheduler(
    private val notificationCenter: UNUserNotificationCenter,
    private val dateManager: DateManager,
) {

    actual fun scheduleNotification(
        id: Int,
        title: String,
        body: String,
        time: Instant
    ) {
        val currentTime = dateManager.fetchCurrentInstant()
        val delayDuration = time - currentTime
        val delay =
            if (delayDuration.isPositive()) {
                delayDuration.inWholeSeconds.coerceAtLeast(1L).toDouble()
            } else {
                1.0
            }

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delay, false)
        val request = UNNotificationRequest.requestWithIdentifier(id.toString(), content, trigger)

        notificationCenter.addNotificationRequest(request) { }
    }

    actual fun scheduleOngoingNotification(
        id: Int,
        title: String,
        body: String,
        time: Instant,
        endTime: Instant,
    ) {
        scheduleNotification(id, title, body, time)
    }

    actual fun updateNotificationGroup(group: String, notificationIds: List<Int>) {
        val defaults = NSUserDefaults.standardUserDefaults
        val storedIds = defaults.stringArrayForKey(group).orEmpty().mapNotNull { value ->
            (value as? String)?.toIntOrNull()
        }
        storedIds.filterNot(notificationIds::contains).forEach(::cancelNotification)
        defaults.setObject(notificationIds.map(Int::toString), forKey = group)
    }

    actual fun clearNotificationGroup(group: String) {
        val defaults = NSUserDefaults.standardUserDefaults
        val storedIds = defaults.stringArrayForKey(group).orEmpty().mapNotNull { value ->
            (value as? String)?.toIntOrNull()
        }
        storedIds.forEach(::cancelNotification)
        defaults.removeObjectForKey(group)
    }

    actual fun cancelNotification(id: Int) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id.toString()))
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf(id.toString()))
    }
}
