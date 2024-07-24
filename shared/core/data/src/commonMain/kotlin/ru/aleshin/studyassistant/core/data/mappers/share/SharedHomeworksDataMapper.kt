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

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.domain.entities.share.ReceivedMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.ReceivedMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.SentMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.SentMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.SharedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.SharedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.remote.models.shared.ReceivedMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ReceivedMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SentMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SentMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SharedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.MediatedHomeworkPojo

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
fun SharedHomeworksPojo.mapToDomain() = SharedHomeworks(
    received = received.map { it.mapToDomain() },
    sent = sent.map { it.mapToDomain() },
)

fun SharedHomeworksDetailsPojo.mapToDomain() = SharedHomeworksDetails(
    received = received.map { it.mapToDomain() },
    sent = sent.map { it.mapToDomain() },
)

fun ReceivedMediatedHomeworksPojo.mapToDomain() = ReceivedMediatedHomeworks(
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender,
    homeworks = homeworks.map { it.mapToDomain() }
)

fun ReceivedMediatedHomeworksDetailsPojo.mapToDomain() = ReceivedMediatedHomeworksDetails(
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    sender = sender.mapToDomain(),
    homeworks = homeworks.map { it.mapToDomain() }
)

fun SentMediatedHomeworksPojo.mapToDomain() = SentMediatedHomeworks(
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipients = recipients,
    homeworks = homeworks.map { it.mapToDomain() }
)

fun SentMediatedHomeworksDetailsPojo.mapToDomain() = SentMediatedHomeworksDetails(
    date = date.mapEpochTimeToInstant(),
    sendDate = sendDate.mapEpochTimeToInstant(),
    recipients = recipients.map { it.mapToDomain() },
    homeworks = homeworks.map { it.mapToDomain() }
)

fun MediatedHomeworkPojo.mapToDomain() = MediatedHomework(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = TaskPriority.valueOf(priority),
)

fun SharedHomeworks.mapToRemoteData() = SharedHomeworksPojo(
    received = received.map { it.mapToRemoteData() },
    sent = sent.map { it.mapToRemoteData() },
)

fun ReceivedMediatedHomeworks.mapToRemoteData() = ReceivedMediatedHomeworksPojo(
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    sender = sender,
    homeworks = homeworks.map { it.mapToRemoteData() }
)

fun SentMediatedHomeworks.mapToRemoteData() = SentMediatedHomeworksPojo(
    date = date.toEpochMilliseconds(),
    sendDate = sendDate.toEpochMilliseconds(),
    recipients = recipients,
    homeworks = homeworks.map { it.mapToRemoteData() }
)

fun MediatedHomework.mapToRemoteData() = MediatedHomeworkPojo(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = priority.name,
)