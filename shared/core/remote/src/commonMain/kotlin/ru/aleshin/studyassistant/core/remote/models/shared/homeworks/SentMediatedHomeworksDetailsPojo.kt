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

package ru.aleshin.studyassistant.core.remote.models.shared.homeworks

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.tasks.MediatedHomeworkPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
@Serializable
data class SentMediatedHomeworksDetailsPojo(
    val uid: UID,
    val date: Long,
    val sendDate: Long,
    val recipients: List<AppUserPojoDetails>,
    val homeworks: List<MediatedHomeworkPojo>,
)