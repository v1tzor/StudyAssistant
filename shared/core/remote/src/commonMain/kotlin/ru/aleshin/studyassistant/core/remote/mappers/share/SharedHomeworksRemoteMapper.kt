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

package ru.aleshin.studyassistant.core.remote.mappers.share

import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.ReceivedMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.ReceivedMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SentMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SentMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
fun SharedHomeworksDetailsPojo.convertToBase() = SharedHomeworksPojo(
    id = id,
    received = received.mapValues { it.value.convertToBase() }.encodeToString(),
    sent = sent.mapValues { it.value.convertToBase() }.encodeToString(),
    updatedAt = updatedAt,
)

fun SharedHomeworksPojo.convertToDetails(
    recipientsMapper: (List<UID>) -> List<AppUserPojoDetails>,
    sendersMapper: (UID) -> AppUserPojoDetails,
) = SharedHomeworksDetailsPojo(
    id = id,
    received = received.decodeFromString<ReceivedMediatedHomeworksPojo>().mapValues {
        it.value.convertToDetails(sendersMapper)
    },
    sent = sent.decodeFromString<SentMediatedHomeworksPojo>().mapValues {
        it.value.convertToDetails(recipientsMapper)
    },
    updatedAt = updatedAt,
)

fun SentMediatedHomeworksDetailsPojo.convertToBase() = SentMediatedHomeworksPojo(
    uid = uid,
    date = date,
    sendDate = sendDate,
    recipients = recipients.map { it.uid },
    homeworks = homeworks,
)

fun SentMediatedHomeworksPojo.convertToDetails(
    recipientsMapper: (List<UID>) -> List<AppUserPojoDetails>,
) = SentMediatedHomeworksDetailsPojo(
    uid = uid,
    date = date,
    sendDate = sendDate,
    recipients = recipientsMapper(recipients),
    homeworks = homeworks,
)

fun ReceivedMediatedHomeworksDetailsPojo.convertToBase() = ReceivedMediatedHomeworksPojo(
    uid = uid,
    date = date,
    sendDate = sendDate,
    sender = sender.uid,
    homeworks = homeworks,
)

fun ReceivedMediatedHomeworksPojo.convertToDetails(
    senderMapper: (UID) -> AppUserPojoDetails,
) = ReceivedMediatedHomeworksDetailsPojo(
    uid = uid,
    date = date,
    sendDate = sendDate,
    sender = senderMapper(sender),
    homeworks = homeworks,
)