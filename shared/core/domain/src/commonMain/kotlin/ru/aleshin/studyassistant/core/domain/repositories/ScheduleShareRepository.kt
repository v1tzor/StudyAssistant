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

import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShare
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShareClaim
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface ScheduleShareRepository {
    suspend fun createShare(share: ScheduleShare): ShareLink
    suspend fun claimShare(code: String): ScheduleShareClaim
    suspend fun confirmShare(claim: ScheduleShareClaim)
    suspend fun releaseShare(claim: ScheduleShareClaim)
    suspend fun importShare(
        organizations: List<Organization>,
        schedules: List<BaseSchedule>,
    )
}
