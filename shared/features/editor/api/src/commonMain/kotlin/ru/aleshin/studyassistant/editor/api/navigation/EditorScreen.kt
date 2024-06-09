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

package ru.aleshin.studyassistant.editor.api.navigation

import functional.UID
import inject.FeatureScreen
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
sealed class EditorScreen : FeatureScreen {

    data object Schedule : EditorScreen()

    data class Class(
        val classId: UID?,
        val scheduleId: UID?,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi,
    ) : EditorScreen()

    data class Subject(val subjectId: UID?, val organizationId: UID) : EditorScreen()

    data class Employee(val employeeId: UID?, val organizationId: UID) : EditorScreen()
}
