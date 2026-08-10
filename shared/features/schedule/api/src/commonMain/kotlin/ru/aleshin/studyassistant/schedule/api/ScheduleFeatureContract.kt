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

package ru.aleshin.studyassistant.schedule.api

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
@Serializable
public sealed class ScheduleConfig {

    @Serializable
    public data object Overview : ScheduleConfig()

    @Serializable
    public data object Details : ScheduleConfig()

    @Serializable
    public data class Share(val code: String? = null) : ScheduleConfig()
}

public sealed class ScheduleOutput : BaseOutput {
    public data object NavigateToBack : ScheduleOutput()

    public sealed class NavigateToEditor : ScheduleOutput() {

        public data class WeekSchedule(
            val week: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
        ) : NavigateToEditor()

        public data class DailySchedule(
            val date: Long,
            val customScheduleId: UID?,
            val baseScheduleId: UID?,
        ) : NavigateToEditor()

        public data class Homework(
            val homeworkId: UID?,
            val date: Long?,
            val subjectId: UID?,
            val organizationId: UID?
        ) : NavigateToEditor()
    }
}
