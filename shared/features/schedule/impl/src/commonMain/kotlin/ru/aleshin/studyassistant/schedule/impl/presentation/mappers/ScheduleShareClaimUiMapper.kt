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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShare
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShareClaim
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ScheduleShareClaimUi

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal fun ScheduleShareClaim.mapToUi() = ScheduleShareClaimUi(
    claimId = claimId,
    senderName = share.senderName,
    schedules = share.schedules.map { it.mapToUi() },
    organizations = share.organizations.map { it.mapToUi() },
)

internal fun ScheduleShareClaimUi.mapToDomain() = ScheduleShareClaim(
    claimId = claimId,
    share = ScheduleShare(
        senderName = senderName,
        schedules = schedules.map { it.mapToDomain() },
        organizations = organizations.map { it.mapToDomain() },
    ),
)
