/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.domain.repositories

import ru.aleshin.studyassistant.core.domain.entities.share.HomeworkShare
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface HomeworkShareRepository {
    suspend fun createShare(share: HomeworkShare): ShareLink
    suspend fun fetchShare(code: String): HomeworkShare
    suspend fun isShareImported(code: String): Boolean
    suspend fun importShare(code: String, homeworks: List<Homework>)
}
