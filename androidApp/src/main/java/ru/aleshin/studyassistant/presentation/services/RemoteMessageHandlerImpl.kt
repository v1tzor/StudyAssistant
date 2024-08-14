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

package ru.aleshin.studyassistant.presentation.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import ru.aleshin.studyassistant.android.R
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.extensions.generateDigitCode
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.domain.entities.message.NotifyPushContent
import ru.aleshin.studyassistant.core.domain.entities.message.NotifyPushContentType
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings
import ru.aleshin.studyassistant.presentation.ui.MainActivity
import ru.rustore.sdk.universalpush.domain.model.UniversalRemoteMessage

/**
 * @author Stanislav Aleshin on 11.08.2024.
 */
class RemoteMessageHandlerImpl(private val context: Context) : RemoteMessageHandler {

    private val notifyCreator by lazy { NotificationCreator.Base(context) }

    override fun handleMessage(message: UniversalRemoteMessage) {
        val defaultNotification = message.notification
        val contentType = message.data[NotifyPushContent.NOTIFY_CONTENT_TYPE]?.let { type ->
            NotifyPushContentType.valueOf(type)
        }

        if (contentType == null && defaultNotification != null) {
            showNotification(
                title = requireNotNull(defaultNotification.title),
                body = requireNotNull(defaultNotification.body),
                color = defaultNotification.color,
                image = defaultNotification.image?.let { Uri.parse(it) },
                channelId = defaultNotification.channelId,
                clickAction = defaultNotification.clickAction,
            )
        } else if (contentType != null) {
            val coreStrings = fetchCoreStrings(fetchAppLanguage(context.fetchCurrentLanguage()))
            when (contentType) {
                NotifyPushContentType.ADD_TO_FRIENDS -> showNotification(
                    title = coreStrings.addToFriendsMessageTitle,
                    body = buildString {
                        val senderName =
                            message.data[NotifyPushContent.AddToFriends.SENDER_NAME]
                        append(senderName ?: coreStrings.emptyMessageUser, " ")
                        append(coreStrings.addToFriendsMessageBodySuffix)
                    },
                )

                NotifyPushContentType.ACCEPT_FRIEND_REQUEST -> showNotification(
                    title = buildString {
                        val senderName =
                            message.data[NotifyPushContent.AcceptFriendRequest.SENDER_NAME]
                        append(senderName ?: coreStrings.emptyMessageUser, " ")
                        append(coreStrings.acceptFriendRequestMessageTitleSuffix)
                    },
                    body = coreStrings.acceptFriendRequestMessageBody,
                )

                NotifyPushContentType.REJECT_FRIEND_REQUEST -> showNotification(
                    title = buildString {
                        val senderName =
                            message.data[NotifyPushContent.RejectFriendRequest.SENDER_NAME]
                        append(senderName ?: coreStrings.emptyMessageUser, " ")
                        append(coreStrings.rejectFriendRequestMessageTitleSuffix)
                    },
                    body = coreStrings.rejectFriendRequestMessageBody,
                )

                NotifyPushContentType.SHARE_HOMEWORK -> showNotification(
                    title = buildString {
                        val senderName =
                            message.data[NotifyPushContent.ShareHomework.SENDER_NAME]
                        append(senderName ?: coreStrings.emptyMessageUser, " ")
                        append(coreStrings.shareHomeworkMessageTitleSuffix)
                    },
                    body = buildString {
                        val subjects = message.data[NotifyPushContent.ShareHomework.SENDER_NAME]
                        append(coreStrings.shareHomeworkMessageBody)
                        append(subjects ?: coreStrings.noneTitle)
                    },
                )

                NotifyPushContentType.SHARE_SCHEDULE -> showNotification(
                    title = buildString {
                        val senderName =
                            message.data[NotifyPushContent.ShareSchedule.SENDER_NAME]
                        append(senderName ?: coreStrings.emptyMessageUser, " ")
                        append(coreStrings.shareScheduleMessageTitleSuffix)
                    },
                    body = coreStrings.shareScheduleMessageBody,
                )
            }
        }
    }

    private fun showNotification(
        id: Int = 1,
        title: String,
        body: String,
        color: String? = null,
        icon: Int = R.drawable.ic_launcher_notification,
        image: Uri? = null,
        channelId: String? = null,
        clickAction: String? = null,
        tag: String? = null,
    ) {
        val contentIntent = clickAction?.let { Intent(it) } ?: Intent(context, MainActivity::class.java)
        val requestCode = generateDigitCode().toInt()
        val pContentIntent = PendingIntent.getActivity(
            context,
            requestCode,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val largeIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && image != null) {
            val source = ImageDecoder.createSource(context.contentResolver, image)
            ImageDecoder.decodeBitmap(source)
        } else if (image != null) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, image)
        } else {
            null
        }
        val notify = notifyCreator.createNotify(
            channelId = channelId ?: Constants.Notification.CHANNEL_ID,
            title = title,
            text = body,
            color = if (color != null) {
                Color.parseColor(color)
            } else {
                null
            },
            smallIcon = icon,
            largeIcon = largeIcon,
            priority = NotificationPriority.MAX,
            contentIntent = pContentIntent,
        )

        notifyCreator.showNotify(notify, id, tag)
    }
}