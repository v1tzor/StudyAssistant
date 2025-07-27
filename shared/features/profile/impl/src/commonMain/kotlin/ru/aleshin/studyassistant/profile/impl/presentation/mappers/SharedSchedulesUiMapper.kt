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

package ru.aleshin.studyassistant.profile.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.share.scheules.ReceivedMediatedSchedulesShort
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SentMediatedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedulesShort
import ru.aleshin.studyassistant.profile.impl.domain.entities.ShareSchedulesSendData
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ReceivedMediatedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SentMediatedSchedulesUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ShareSchedulesSendDataUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SharedSchedulesShortUi

/**
 * @author Stanislav Aleshin on 15.08.2024.
 */
internal fun SharedSchedulesShort.mapToUi() = SharedSchedulesShortUi(
    sent = sent.mapValues { it.value.mapToUi() },
    received = received.mapValues { it.value.mapToUi() },
    updatedAt = updatedAt,
)

internal fun ReceivedMediatedSchedulesShort.mapToUi() = ReceivedMediatedSchedulesShortUi(
    uid = uid,
    sendDate = sendDate,
    sender = sender.mapToUi(),
    organizationNames = organizationNames,
)

internal fun SentMediatedSchedules.mapToUi() = SentMediatedSchedulesUi(
    uid = uid,
    sendDate = sendDate,
    recipient = recipient.mapToUi(),
    organizationNames = organizationNames,
)

internal fun SentMediatedSchedulesUi.mapToDomain() = SentMediatedSchedules(
    uid = uid,
    sendDate = sendDate,
    recipient = recipient.mapToDomain(),
    organizationNames = organizationNames,
)

internal fun ShareSchedulesSendDataUi.mapToDomain() = ShareSchedulesSendData(
    recipient = recipient.mapToDomain(),
    organizations = organizations,
    sendAllSubjects = sendAllSubjects,
    sendAllEmployee = sendAllEmployee,
)