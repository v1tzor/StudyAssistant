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

package ru.aleshin.studyassistant.core.common.messages

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import ru.aleshin.studyassistant.core.common.R
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationChronometer
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority

/**
 * @author Stanislav Aleshin on 27.08.2024.
 */
class LocalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val title = intent.getStringExtra(TITLE_KEY)
        val body = intent.getStringExtra(BODY_KEY)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, 0)

        if (
            intent.action == INTENT_ACTION &&
            title != null &&
            body != null &&
            intent.hasExtra(NOTIFICATION_ID_KEY)
        ) {
            val notificationCreator = context.let { NotificationCreator.Base(it) }
            val mainActivityUri = Uri.parse(Constants.App.OPEN_APP_DEEPLINK)
            val contentIntent = Intent(ACTION_VIEW, mainActivityUri)
            val pContentIntent =
                PendingIntent.getActivity(context, notificationId, contentIntent, FLAG_IMMUTABLE)
            val isOngoing = intent.getBooleanExtra(ONGOING_KEY, false)
            val endTime = intent.getLongExtra(END_TIME_KEY, 0L)
            if (isOngoing && endTime <= System.currentTimeMillis()) {
                cancelNotification(context, notificationId)
                return
            }
            val notify = notificationCreator.createNotify(
                channelId = if (isOngoing) {
                    Constants.Notification.ONGOING_CHANNEL_ID
                } else {
                    Constants.Notification.CHANNEL_ID
                },
                title = title,
                text = body,
                priority = if (isOngoing) NotificationPriority.LOW else NotificationPriority.MAX,
                category = NotificationCategory.CATEGORY_REMINDER,
                smallIcon = R.drawable.ic_launcher_notification,
                contentIntent = pContentIntent,
                autoCancel = !isOngoing,
                silent = isOngoing,
                ongoing = isOngoing,
                chronometer = endTime.takeIf { isOngoing }?.let { targetTime ->
                    NotificationChronometer(whenMillis = targetTime, countDown = true)
                },
                timeoutAfterMillis = endTime.takeIf { isOngoing }?.let { targetTime ->
                    targetTime - System.currentTimeMillis()
                },
            )

            notificationCreator.showNotify(
                notification = notify,
                notifyId = notificationId,
                tag = Constants.Notification.ONGOING_TAG.takeIf { isOngoing },
            )
        }
    }

    companion object {
        const val INTENT_ACTION = "ru.aleshin.studyassistant.ALARM_NOTIFICATION_ACTION"
        const val TITLE_KEY = "SCHEDULED_NOTIFICATION_TITLE"
        const val BODY_KEY = "SCHEDULED_NOTIFICATION_BODY"
        const val NOTIFICATION_ID_KEY = "SCHEDULED_NOTIFICATION_ID"
        const val ONGOING_KEY = "SCHEDULED_NOTIFICATION_ONGOING"
        const val END_TIME_KEY = "SCHEDULED_NOTIFICATION_END_TIME"

        fun createIntent(context: Context, id: Int, title: String, body: String): Intent {
            return Intent(context, LocalNotificationReceiver::class.java).apply {
                action = INTENT_ACTION
                putExtra(NOTIFICATION_ID_KEY, id)
                putExtra(TITLE_KEY, title)
                putExtra(BODY_KEY, body)
            }
        }

        fun createOngoingIntent(
            context: Context,
            id: Int,
            title: String,
            body: String,
            endTime: Long,
        ): Intent {
            return createIntent(context, id, title, body).apply {
                putExtra(ONGOING_KEY, true)
                putExtra(END_TIME_KEY, endTime)
            }
        }

        fun createCancelIntent(context: Context): Intent {
            return Intent(context, LocalNotificationReceiver::class.java).apply {
                action = INTENT_ACTION
            }
        }

        fun cancelNotification(context: Context, id: Int) {
            NotificationCreator.Base(context).apply {
                cancelNotify(id)
                cancelNotify(id, Constants.Notification.ONGOING_TAG)
            }
        }
    }
}
