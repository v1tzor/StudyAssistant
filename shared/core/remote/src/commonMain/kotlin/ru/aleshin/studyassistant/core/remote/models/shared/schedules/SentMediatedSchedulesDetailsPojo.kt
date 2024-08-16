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

package ru.aleshin.studyassistant.core.remote.models.shared.schedules

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
@Serializable
data class SentMediatedSchedulesDetailsPojo(
    val uid: UID,
    val sendDate: Long,
    val recipient: AppUserPojo,
    val organizationNames: List<String>,
)