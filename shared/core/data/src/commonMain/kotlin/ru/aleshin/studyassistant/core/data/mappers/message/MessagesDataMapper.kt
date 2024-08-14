/*
 * Copyright 2023 Stanislav Aleshin
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
 * imitations under the License.
 */

package ru.aleshin.studyassistant.core.data.mappers.message

import ru.aleshin.studyassistant.core.domain.entities.message.Message
import ru.aleshin.studyassistant.core.domain.entities.message.PushStrategy
import ru.aleshin.studyassistant.core.remote.models.message.UniversalMessageData
import ru.aleshin.studyassistant.core.remote.models.message.UniversalPushMessageBodyPojo
import ru.aleshin.studyassistant.core.remote.models.message.UniversalPushNotificationPojo

/**
 * @author Stanislav Aleshin on 08.09.2023.
 */
fun Message.mapToData() = when (this) {
    is Message.Notification -> UniversalMessageData(
        tokens = (pushStrategy as? PushStrategy.Token)?.values?.groupBy { it.pushServiceType }?.mapValues { entry ->
            entry.value.map { it.value }
        },
        topic = (pushStrategy as? PushStrategy.Topic)?.value,
        message = UniversalPushMessageBodyPojo(
            data = data,
            notification = UniversalPushNotificationPojo(
                title = title,
                body = body,
                image = image,
            ),
        )
    )
    is Message.Data -> UniversalMessageData(
        tokens = (pushStrategy as? PushStrategy.Token)?.values?.groupBy { it.pushServiceType }?.mapValues { entry ->
            entry.value.map { it.value }
        },
        topic = (pushStrategy as? PushStrategy.Topic)?.value,
        message = UniversalPushMessageBodyPojo(
            data = data,
        )
    )
}