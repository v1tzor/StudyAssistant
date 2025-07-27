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

package ru.aleshin.studyassistant.core.data.mappers.share

import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToRemoteData
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToRemoteData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.utils.sync.SingleSyncMapper
import ru.aleshin.studyassistant.core.database.mappers.user.convertToDetails
import ru.aleshin.studyassistant.core.database.models.shared.schedules.ReceivedMediatedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.schedules.SentMediatedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.schedules.SharedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.ReceivedMediatedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.ReceivedMediatedSchedulesShort
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SentMediatedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedulesShort
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortDetailsPojo

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
class SharedSchedulesSyncMapper : SingleSyncMapper<SharedSchedulesShortDetailsEntity, SharedSchedulesDetailsPojo>(
    localToRemote = { error("Not support") },
    remoteToLocal = { mapToDomainShort().mapToLocalData() }
)

fun SharedSchedulesDetailsPojo.mapToDomain() = SharedSchedules(
    sent = sent.mapValues { it.value.mapToDomain() },
    received = received.mapValues { it.value.mapToDomain() },
    updatedAt = updatedAt,
)

fun SharedSchedulesDetailsPojo.mapToDomainShort() = SharedSchedulesShort(
    sent = sent.mapValues { it.value.mapToDomain() },
    received = received.mapValues { it.value.mapToDomainShort() },
    updatedAt = updatedAt,
)

fun SharedSchedulesShortDetailsPojo.mapToDomain() = SharedSchedulesShort(
    sent = sent.mapValues { it.value.mapToDomain() },
    received = received.mapValues { it.value.mapToDomain() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedSchedulesDetailsPojo.mapToDomain() = ReceivedMediatedSchedules(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    schedules = schedules.map { it.mapToDomain() },
    organizationsData = organizationsData.map { it.mapToDomain() },
)

fun ReceivedMediatedSchedulesDetailsPojo.mapToDomainShort() = ReceivedMediatedSchedulesShort(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    organizationNames = organizationsData.map { it.shortName },
)

fun ReceivedMediatedSchedulesShortDetailsPojo.mapToDomain() = ReceivedMediatedSchedulesShort(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    organizationNames = organizationNames,
)

fun SentMediatedSchedulesDetailsPojo.mapToDomain() = SentMediatedSchedules(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipient = recipient.mapToDomain(),
    organizationNames = organizationNames,
)

fun SentMediatedSchedulesShortDetailsPojo.mapToDomain() = SentMediatedSchedules(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipient = recipient.mapToDomain(),
    organizationNames = organizationNames,
)

fun SharedSchedules.mapToRemoteData(userId: String) = SharedSchedulesPojo(
    id = userId,
    sent = sent.mapValues { it.value.mapToRemoteData() }.encodeToString(),
    received = received.mapValues { it.value.mapToRemoteData() }.encodeToString(),
    updatedAt = updatedAt,
)

//fun SharedSchedules.mapToRemoteData(userId: String) = SharedSchedulesDetailsPojo(
//    id = userId,
//    sent = sent.mapValues { it.value.mapToRemoteData() }.encodeToString(),
//    received = received.mapValues { it.value.mapToRemoteData() }.encodeToString(),
//    updatedAt = updatedAt,
//)

fun ReceivedMediatedSchedules.mapToRemoteData() = ReceivedMediatedSchedulesPojo(
    uid = uid,
    sendDate = sendDate.toEpochMilliseconds(),
    sender = sender.uid,
    schedules = schedules.map { it.mapToRemoteData() },
    organizationsData = organizationsData.map { it.mapToRemoteData() },
)

fun SentMediatedSchedules.mapToRemoteData() = SentMediatedSchedulesPojo(
    uid = uid,
    sendDate = sendDate.toEpochMilliseconds(),
    recipient = recipient.uid,
    organizationNames = organizationNames,
)

fun SharedSchedulesShort.mapToLocalData() = SharedSchedulesShortDetailsEntity(
    uid = "1",
    sent = sent.mapValues { it.value.mapToLocalData() },
    received = received.mapValues { it.value.mapToLocalData() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedSchedulesShort.mapToLocalData() = ReceivedMediatedSchedulesShortDetailsEntity(
    uid = uid,
    sendDate = sendDate.toEpochMilliseconds(),
    sender = sender.mapToLocalData().convertToDetails(),
    organizationNames = organizationNames,
)

fun SentMediatedSchedules.mapToLocalData() = SentMediatedSchedulesShortDetailsEntity(
    uid = uid,
    sendDate = sendDate.toEpochMilliseconds(),
    recipient = recipient.mapToLocalData().convertToDetails(),
    organizationNames = organizationNames,
)

fun SharedSchedulesShortDetailsEntity.mapToDomain() = SharedSchedulesShort(
    sent = sent.mapValues { it.value.mapToDomain() },
    received = received.mapValues { it.value.mapToDomain() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedSchedulesShortDetailsEntity.mapToDomain() = ReceivedMediatedSchedulesShort(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    organizationNames = organizationNames,
)

fun SentMediatedSchedulesShortDetailsEntity.mapToDomain() = SentMediatedSchedules(
    uid = uid,
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipient = recipient.mapToDomain(),
    organizationNames = organizationNames,
)