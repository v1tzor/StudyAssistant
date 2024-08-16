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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.share

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.MediatedOrganizationUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.MediatedBaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.AppUserUi

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
@Parcelize
internal data class ReceivedMediatedSchedulesUi(
    val uid: UID,
    @TypeParceler<Instant, InstantParceler>
    val sendDate: Instant,
    val sender: AppUserUi,
    val schedules: List<MediatedBaseScheduleUi>,
    val organizationsData: List<MediatedOrganizationUi>,
) : Parcelable