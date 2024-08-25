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

package ru.aleshin.studyassistant.core.domain.entities.share.homeworks

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
data class SentMediatedHomeworksDetails(
    val uid: UID,
    val date: Instant,
    val sendDate: Instant,
    val recipients: List<AppUser>,
    val homeworks: List<MediatedHomework>,
)

fun SentMediatedHomeworksDetails.convertToBase() = SentMediatedHomeworks(
    uid = uid,
    date = date,
    sendDate = sendDate,
    recipients = recipients.map { it.uid },
    homeworks = homeworks,
)