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

import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToRemoteData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteDataDetails
import ru.aleshin.studyassistant.core.data.utils.sync.SingleSyncMapper
import ru.aleshin.studyassistant.core.database.mappers.user.convertToDetails
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.ReceivedMediatedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.SentMediatedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.SharedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.ReceivedMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.ReceivedMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SentMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SentMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworksDetails
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.ReceivedMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.ReceivedMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SentMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SentMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksPojo

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */

// Remote

fun SharedHomeworks.mapToRemoteData(userId: String) = SharedHomeworksPojo(
    id = userId,
    received = received.mapValues { it.value.mapToRemoteData() }.encodeToString(),
    sent = sent.mapValues { it.value.mapToRemoteData() }.encodeToString(),
    updatedAt = updatedAt,
)

fun SharedHomeworksDetails.mapToRemoteData(userId: String) = SharedHomeworksDetailsPojo(
    id = userId,
    received = received.mapValues { it.value.mapToRemoteData() },
    sent = sent.mapValues { it.value.mapToRemoteData() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedHomeworks.mapToRemoteData() = ReceivedMediatedHomeworksPojo(
    uid = uid,
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    sender = sender,
    homeworks = homeworks.map { it.mapToRemoteData() }
)

fun SentMediatedHomeworks.mapToRemoteData() = SentMediatedHomeworksPojo(
    uid = uid,
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    recipients = recipients,
    homeworks = homeworks.map { it.mapToRemoteData() }
)

fun ReceivedMediatedHomeworksDetails.mapToRemoteData() = ReceivedMediatedHomeworksDetailsPojo(
    uid = uid,
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    sender = sender.mapToRemoteDataDetails(),
    homeworks = homeworks.map { it.mapToRemoteData() }
)

fun SentMediatedHomeworksDetails.mapToRemoteData() = SentMediatedHomeworksDetailsPojo(
    uid = uid,
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    recipients = recipients.map { it.mapToRemoteDataDetails() },
    homeworks = homeworks.map { it.mapToRemoteData() }
)

fun SharedHomeworksPojo.mapToDomain() = SharedHomeworks(
    received = received.decodeFromString<UID, ReceivedMediatedHomeworksPojo>().mapValues { it.value.mapToDomain() },
    sent = sent.decodeFromString<UID, SentMediatedHomeworksPojo>().mapValues { it.value.mapToDomain() },
    updatedAt = updatedAt,
)

fun SharedHomeworksDetailsPojo.mapToDomain() = SharedHomeworksDetails(
    received = received.mapValues { it.value.mapToDomain() },
    sent = sent.mapValues { it.value.mapToDomain() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedHomeworksPojo.mapToDomain() = ReceivedMediatedHomeworks(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender,
    homeworks = homeworks.map { it.mapToDomain() }
)

fun SentMediatedHomeworksPojo.mapToDomain() = SentMediatedHomeworks(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipients = recipients,
    homeworks = homeworks.map { it.mapToDomain() }
)

fun ReceivedMediatedHomeworksDetailsPojo.mapToDomain() = ReceivedMediatedHomeworksDetails(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    homeworks = homeworks.map { it.mapToDomain() }
)

fun SentMediatedHomeworksDetailsPojo.mapToDomain() = SentMediatedHomeworksDetails(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipients = recipients.map { it.mapToDomain() },
    homeworks = homeworks.map { it.mapToDomain() }
)

// Local

fun SharedHomeworksDetails.mapToLocalData() = SharedHomeworksDetailsEntity(
    uid = "1",
    received = received.mapValues { it.value.mapToLocalData() },
    sent = sent.mapValues { it.value.mapToLocalData() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedHomeworksDetails.mapToLocalData() = ReceivedMediatedHomeworksDetailsEntity(
    uid = uid,
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    sender = sender.mapToLocalData().convertToDetails(),
    homeworks = homeworks.map { it.mapToLocalData() }
)

fun SentMediatedHomeworksDetails.mapToLocalData() = SentMediatedHomeworksDetailsEntity(
    uid = uid,
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    recipients = recipients.map { it.mapToLocalData().convertToDetails() },
    homeworks = homeworks.map { it.mapToLocalData() }
)

fun SharedHomeworksDetailsEntity.mapToDomain() = SharedHomeworksDetails(
    received = received.mapValues { it.value.mapToDomain() },
    sent = sent.mapValues { it.value.mapToDomain() },
    updatedAt = updatedAt,
)

fun ReceivedMediatedHomeworksDetailsEntity.mapToDomain() = ReceivedMediatedHomeworksDetails(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    homeworks = homeworks.map { it.mapToDomain() }
)

fun SentMediatedHomeworksDetailsEntity.mapToDomain() = SentMediatedHomeworksDetails(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipients = recipients.map { it.mapToDomain() },
    homeworks = homeworks.map { it.mapToDomain() }
)

// Mapper

class ShareHomeworksSyncMapper : SingleSyncMapper<SharedHomeworksDetailsEntity, SharedHomeworksDetailsPojo>(
    localToRemote = { mapToDomain().mapToRemoteData("") },
    remoteToLocal = { mapToDomain().mapToLocalData() },
)