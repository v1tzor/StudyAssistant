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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.friday_title as core_friday_title
import ru.aleshin.studyassistant.core.ui.resources.monday_title as core_monday_title
import ru.aleshin.studyassistant.core.ui.resources.one_week_plural as core_one_week_plural
import ru.aleshin.studyassistant.core.ui.resources.saturday_title as core_saturday_title
import ru.aleshin.studyassistant.core.ui.resources.sunday_title as core_sunday_title
import ru.aleshin.studyassistant.core.ui.resources.three_week_plural as core_three_week_plural
import ru.aleshin.studyassistant.core.ui.resources.thursday_title as core_thursday_title
import ru.aleshin.studyassistant.core.ui.resources.tuesday_title as core_tuesday_title
import ru.aleshin.studyassistant.core.ui.resources.two_week_plural as core_two_week_plural
import ru.aleshin.studyassistant.core.ui.resources.wednesday_title as core_wednesday_title

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Composable
fun NumberOfRepeatWeek.mapToSting() = when (this) {
    NumberOfRepeatWeek.ONE -> stringResource(CoreRes.string.core_one_week_plural)
    NumberOfRepeatWeek.TWO -> stringResource(CoreRes.string.core_two_week_plural)
    NumberOfRepeatWeek.THREE -> stringResource(CoreRes.string.core_three_week_plural)
}

@Composable
fun DayOfWeek.mapToSting() = when (this) {
    DayOfWeek.MONDAY -> stringResource(CoreRes.string.core_monday_title)
    DayOfWeek.TUESDAY -> stringResource(CoreRes.string.core_tuesday_title)
    DayOfWeek.WEDNESDAY -> stringResource(CoreRes.string.core_wednesday_title)
    DayOfWeek.THURSDAY -> stringResource(CoreRes.string.core_thursday_title)
    DayOfWeek.FRIDAY -> stringResource(CoreRes.string.core_friday_title)
    DayOfWeek.SATURDAY -> stringResource(CoreRes.string.core_saturday_title)
    DayOfWeek.SUNDAY -> stringResource(CoreRes.string.core_sunday_title)
    else -> error("Unknown day of week: $this")
}