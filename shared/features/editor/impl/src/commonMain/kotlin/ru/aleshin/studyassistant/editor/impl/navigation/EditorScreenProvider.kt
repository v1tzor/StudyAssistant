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

package ru.aleshin.studyassistant.editor.impl.navigation

import navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.ClassEditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.EmployeeEditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.HomeworkEditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.OrganizationEditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.ProfileEditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.ScheduleEditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.SubjectEditorScreen

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface EditorScreenProvider : FeatureScreenProvider<EditorScreen> {

    class Base : EditorScreenProvider {

        override fun provideFeatureScreen(screen: EditorScreen) = when (screen) {
            is EditorScreen.Schedule -> ScheduleEditorScreen(
                week = screen.week,
            )
            is EditorScreen.Class -> ClassEditorScreen(
                classId = screen.classId,
                scheduleId = screen.scheduleId,
                customSchedule = screen.isCustomSchedule,
                weekDay = screen.weekDay,
            )
            is EditorScreen.Subject -> SubjectEditorScreen(
                subjectId = screen.subjectId,
                organizationId = screen.organizationId,
            )
            is EditorScreen.Employee -> EmployeeEditorScreen(
                employeeId = screen.employeeId,
                organizationId = screen.organizationId,
            )
            is EditorScreen.Homework -> HomeworkEditorScreen(
                homeworkId = screen.homeworkId,
            )
            is EditorScreen.Organization -> OrganizationEditorScreen(
                organizationId = screen.organizationId,
            )
            is EditorScreen.Profile -> ProfileEditorScreen()
        }
    }
}