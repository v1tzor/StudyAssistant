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

package ru.aleshin.studyassistant.core.domain.entities.message

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice

/**
 * @author Stanislav Aleshin on 05.08.2024.
 */
sealed class NotifyPushContent {

    abstract val type: NotifyPushContentType

    abstract fun toMessageBody(): Message

    data class AddToFriends(
        val devices: List<UserDevice>,
        val senderUsername: String,
        val senderUserId: UID,
    ) : NotifyPushContent() {

        override val type = NotifyPushContentType.ADD_TO_FRIENDS

        override fun toMessageBody() = Message.Data(
            pushStrategy = PushStrategy.Token(
                values = devices.map { PushValue(it.pushToken, it.pushServiceType) }
            ),
            data = mapOf(
                NOTIFY_CONTENT_TYPE to type.toString(),
                SENDER_NAME to senderUsername,
                SENDER_ID to senderUserId
            )
        )

        companion object {
            const val SENDER_NAME = "friend_request_sender_name"
            const val SENDER_ID = "friend_request_sender_id"
        }
    }

    data class AcceptFriendRequest(
        val devices: List<UserDevice>,
        val senderUsername: String,
        val senderUserId: UID,
    ) : NotifyPushContent() {

        override val type = NotifyPushContentType.ACCEPT_FRIEND_REQUEST

        override fun toMessageBody() = Message.Data(
            pushStrategy = PushStrategy.Token(
                values = devices.map { PushValue(it.pushToken, it.pushServiceType) }
            ),
            data = mapOf(
                NOTIFY_CONTENT_TYPE to type.toString(),
                SENDER_NAME to senderUsername,
                SENDER_ID to senderUserId,
            )
        )

        companion object {
            const val SENDER_NAME = "new_friend_name"
            const val SENDER_ID = "new_friend_id"
        }
    }

    data class RejectFriendRequest(
        val devices: List<UserDevice>,
        val senderUsername: String,
        val senderUserId: UID,
    ) : NotifyPushContent() {

        override val type = NotifyPushContentType.REJECT_FRIEND_REQUEST

        override fun toMessageBody() = Message.Data(
            pushStrategy = PushStrategy.Token(
                values = devices.map { PushValue(it.pushToken, it.pushServiceType) }
            ),
            data = mapOf(
                NOTIFY_CONTENT_TYPE to type.toString(),
                SENDER_NAME to senderUsername,
                SENDER_ID to senderUserId,
            )
        )

        companion object {
            const val SENDER_NAME = "rejected_sender_name"
            const val SENDER_ID = "rejected_sender_id"
        }
    }

    data class ShareHomework(
        val devices: List<UserDevice>,
        val senderUsername: String,
        val senderUserId: UID,
        val subjectNames: List<String>,
    ) : NotifyPushContent() {

        override val type = NotifyPushContentType.SHARE_HOMEWORK

        override fun toMessageBody() = Message.Data(
            PushStrategy.Token(
                values = devices.map { PushValue(it.pushToken, it.pushServiceType) }
            ),
            data = mapOf(
                NOTIFY_CONTENT_TYPE to type.toString(),
                SENDER_NAME to senderUsername,
                SENDER_ID to senderUserId,
                SUBJECT_NAMES to subjectNames.joinToString(),
            )
        )

        companion object {
            const val SENDER_NAME = "homework_sender_name"
            const val SENDER_ID = "homework_sender_id"
            const val SUBJECT_NAMES = "homework_subjects"
        }
    }

    data class ShareSchedule(
        val devices: List<UserDevice>,
        val senderUsername: String,
        val senderUserId: UID,
    ) : NotifyPushContent() {

        override val type = NotifyPushContentType.SHARE_SCHEDULE

        override fun toMessageBody() = Message.Data(
            PushStrategy.Token(
                values = devices.map { PushValue(it.pushToken, it.pushServiceType) }
            ),
            data = mapOf(
                NOTIFY_CONTENT_TYPE to type.toString(),
                SENDER_NAME to senderUsername,
                SENDER_ID to senderUserId,
            )
        )

        companion object {
            const val SENDER_NAME = "schedule_sender_name"
            const val SENDER_ID = "schedule_sender_id"
        }
    }

    companion object {
        const val NOTIFY_CONTENT_TYPE = "notify_push_content_type"
    }
}

enum class NotifyPushContentType {
    ADD_TO_FRIENDS,
    ACCEPT_FRIEND_REQUEST,
    REJECT_FRIEND_REQUEST,
    SHARE_HOMEWORK,
    SHARE_SCHEDULE
}