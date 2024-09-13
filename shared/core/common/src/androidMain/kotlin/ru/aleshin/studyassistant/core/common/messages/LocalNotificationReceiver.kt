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

package ru.aleshin.studyassistant.core.common.messages

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import ru.aleshin.studyassistant.core.common.R
import ru.aleshin.studyassistant.core.common.extensions.generateDigitCode
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority

/**
 * @author Stanislav Aleshin on 27.08.2024.
 */
class LocalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val title = intent.getStringExtra(TITLE_KEY)
        val body = intent.getStringExtra(BODY_KEY)

        if (intent.action == INTENT_ACTION && title != null && body != null) {
            val notificationCreator = context.let { NotificationCreator.Base(it) }
            val mainActivityUri = Uri.parse("app://studyassistant.com/openMain")
            val contentIntent = Intent(ACTION_VIEW, mainActivityUri)
            val requestCode = generateDigitCode().toInt()
            val pContentIntent = PendingIntent.getActivity(context, requestCode, contentIntent, FLAG_IMMUTABLE)
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = title,
                text = body,
                priority = NotificationPriority.MAX,
                category = NotificationCategory.CATEGORY_REMINDER,
                smallIcon = R.drawable.ic_launcher_notification,
                contentIntent = pContentIntent,
            )

            notificationCreator.showNotify(notify)
        }
    }

    companion object {
        const val INTENT_ACTION = "ru.aleshin.studyassistant.ALARM_NOTIFICATION_ACTION"
        const val TITLE_KEY = "SCHEDULED_NOTIFICATION_TITLE"
        const val BODY_KEY = "SCHEDULED_NOTIFICATION_BODY"

        fun createIntent(context: Context, title: String, body: String): Intent {
            return Intent(context, LocalNotificationReceiver::class.java).apply {
                action = INTENT_ACTION
                putExtra(TITLE_KEY, title)
                putExtra(BODY_KEY, body)
            }
        }

        fun createCancelIntent(context: Context): Intent {
            return Intent(context, LocalNotificationReceiver::class.java).apply {
                action = INTENT_ACTION
            }
        }
    }
}