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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.share.ReceivedMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.ReceivedMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.SentMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.SentMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.SharedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.SharedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTasksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkUi

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal fun SharedHomeworksDetails.mapToUi() = SharedHomeworksDetailsUi(
    received = received.map { it.mapToUi() },
    sent = sent.map { it.mapToUi() }
)

internal fun SharedHomeworks.mapToUi() = SharedHomeworksUi(
    received = received.map { it.mapToUi() },
    sent = sent.map { it.mapToUi() }
)

internal fun ReceivedMediatedHomeworksDetails.mapToUi() = ReceivedMediatedHomeworksDetailsUi(
    date = date,
    sendDate = sendDate,
    sender = sender.mapToUi(),
    homeworks = homeworks.map { it.mapToUi() }
)

internal fun ReceivedMediatedHomeworks.mapToUi() = ReceivedMediatedHomeworksUi(
    date = date,
    sendDate = sendDate,
    sender = sender,
    homeworks = homeworks.map { it.mapToUi() }
)

internal fun SentMediatedHomeworksDetails.mapToUi() = SentMediatedHomeworksDetailsUi(
    date = date,
    sendDate = sendDate,
    recipients = recipients.map { it.mapToUi() },
    homeworks = homeworks.map { it.mapToUi() }
)

internal fun SentMediatedHomeworks.mapToUi() = SentMediatedHomeworksUi(
    date = date,
    sendDate = sendDate,
    recipients = recipients,
    homeworks = homeworks.map { it.mapToUi() }
)

internal fun MediatedHomework.mapToUi() = MediatedHomeworkUi(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = HomeworkTasksUi(
        origin = theoreticalTasks,
        components = theoreticalTasks.toHomeworkComponents().map { it.mapToUi() }
    ),
    practicalTasks = HomeworkTasksUi(
        origin = practicalTasks,
        components = practicalTasks.toHomeworkComponents().map { it.mapToUi() }
    ),
    presentationTasks = HomeworkTasksUi(
        origin = presentationTasks,
        components = presentationTasks.toHomeworkComponents().map { it.mapToUi() }
    ),
    test = test,
    priority = priority,
)

internal fun SharedHomeworksDetailsUi.mapToDomain() = SharedHomeworksDetails(
    received = received.map { it.mapToDomain() },
    sent = sent.map { it.mapToDomain() }
)

internal fun SharedHomeworksUi.mapToDomain() = SharedHomeworks(
    received = received.map { it.mapToDomain() },
    sent = sent.map { it.mapToDomain() }
)

internal fun ReceivedMediatedHomeworksDetailsUi.mapToDomain() = ReceivedMediatedHomeworksDetails(
    date = date,
    sendDate = sendDate,
    sender = sender.mapToDomain(),
    homeworks = homeworks.map { it.mapToDomain() }
)

internal fun ReceivedMediatedHomeworksUi.mapToDomain() = ReceivedMediatedHomeworks(
    date = date,
    sendDate = sendDate,
    sender = sender,
    homeworks = homeworks.map { it.mapToDomain() }
)

internal fun SentMediatedHomeworksDetailsUi.mapToDomain() = SentMediatedHomeworksDetails(
    date = date,
    sendDate = sendDate,
    recipients = recipients.map { it.mapToDomain() },
    homeworks = homeworks.map { it.mapToDomain() }
)

internal fun SentMediatedHomeworksUi.mapToDomain() = SentMediatedHomeworks(
    date = date,
    sendDate = sendDate,
    recipients = recipients,
    homeworks = homeworks.map { it.mapToDomain() }
)

internal fun MediatedHomeworkUi.mapToDomain() = MediatedHomework(
    uid = uid,
    subjectName = subjectName,
    theoreticalTasks = theoreticalTasks.origin,
    practicalTasks = practicalTasks.origin,
    presentationTasks = presentationTasks.origin,
    test = test,
    priority = priority,
)