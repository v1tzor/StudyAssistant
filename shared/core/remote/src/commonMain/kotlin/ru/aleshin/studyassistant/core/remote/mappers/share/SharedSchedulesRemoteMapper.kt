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
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesShortPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
fun SharedSchedulesDetailsPojo.convertToBase() = SharedSchedulesPojo(
    id = id,
    sent = sent.mapValues { it.value.convertToBase() }.encodeToString(),
    received = received.mapValues { it.value.convertToBase() }.encodeToString(),
    updatedAt = updatedAt,
)

fun SharedSchedulesPojo.convertToDetails(
    recipientMapper: (UID) -> AppUserPojoDetails,
    senderMapper: (UID) -> AppUserPojoDetails,
) = SharedSchedulesDetailsPojo(
    id = id,
    sent = sent.decodeFromString<UID, SentMediatedSchedulesPojo>().mapValues {
        it.value.convertToDetails(recipientMapper)
    },
    received = received.decodeFromString<UID, ReceivedMediatedSchedulesPojo>().mapValues {
        it.value.convertToDetails(senderMapper(it.value.sender))
    },
    updatedAt = updatedAt,
)

fun SharedSchedulesShortPojo.convertToShortDetails(
    recipientMapper: (UID) -> AppUserPojoDetails,
    senderMapper: (UID) -> AppUserPojoDetails,
) = SharedSchedulesShortDetailsPojo(
    id = id,
    sent = sent.decodeFromString<UID, SentMediatedSchedulesPojo>().mapValues {
        it.value.convertToShortDetails(recipientMapper)
    },
    received = received.decodeFromString<UID, ReceivedMediatedSchedulesShortPojo>().mapValues {
        it.value.convertToShortDetails(senderMapper(it.value.sender))
    },
    updatedAt = updatedAt,
)

fun SentMediatedSchedulesPojo.convertToDetails(
    recipientMapper: (UID) -> AppUserPojoDetails,
) = SentMediatedSchedulesDetailsPojo(
    uid = uid,
    sendDate = sendDate,
    recipient = recipientMapper(recipient),
    organizationNames = organizationNames,
)

fun SentMediatedSchedulesDetailsPojo.convertToBase() = SentMediatedSchedulesPojo(
    uid = uid,
    sendDate = sendDate,
    recipient = recipient.uid,
    organizationNames = organizationNames,
)

fun SentMediatedSchedulesPojo.convertToShortDetails(
    recipientMapper: (UID) -> AppUserPojoDetails,
) = SentMediatedSchedulesShortDetailsPojo(
    uid = uid,
    sendDate = sendDate,
    recipient = recipientMapper(recipient),
    organizationNames = organizationNames,
)

fun ReceivedMediatedSchedulesDetailsPojo.convertToBase() = ReceivedMediatedSchedulesPojo(
    uid = uid,
    sendDate = sendDate,
    sender = sender.uid,
    schedules = schedules,
    organizationsData = organizationsData,
)

fun ReceivedMediatedSchedulesPojo.convertToDetails(
    sender: AppUserPojoDetails,
) = ReceivedMediatedSchedulesDetailsPojo(
    uid = uid,
    sendDate = sendDate,
    sender = sender,
    schedules = schedules,
    organizationsData = organizationsData,
)

fun ReceivedMediatedSchedulesShortPojo.convertToShortDetails(
    sender: AppUserPojoDetails,
) = ReceivedMediatedSchedulesShortDetailsPojo(
    uid = uid,
    sendDate = sendDate,
    sender = sender,
    organizationNames = organizationsData.map { it.shortName },
)