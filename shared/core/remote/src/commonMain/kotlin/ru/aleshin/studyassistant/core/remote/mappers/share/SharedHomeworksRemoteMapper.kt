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

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.shared.ReceivedMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ReceivedMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SentMediatedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SentMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.SharedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
fun SharedHomeworksPojo.convertToDetails(
    recipientsMapper: (List<UID>) -> List<AppUserPojo>,
    sendersMapper: (UID) -> AppUserPojo,
) = SharedHomeworksDetailsPojo(
    received = received.map { it.convertToDetails(sendersMapper) },
    sent = sent.map { it.convertToDetails(recipientsMapper) }
)

fun SentMediatedHomeworksPojo.convertToDetails(
    recipientsMapper: (List<UID>) -> List<AppUserPojo>,
) = SentMediatedHomeworksDetailsPojo(
    date = date,
    sendDate = sendDate,
    recipients = recipientsMapper(recipients),
    homeworks = homeworks,
)

fun ReceivedMediatedHomeworksPojo.convertToDetails(
    senderMapper: (UID) -> AppUserPojo,
) = ReceivedMediatedHomeworksDetailsPojo(
    date = date,
    sendDate = sendDate,
    sender = senderMapper(sender),
    homeworks = homeworks,
)