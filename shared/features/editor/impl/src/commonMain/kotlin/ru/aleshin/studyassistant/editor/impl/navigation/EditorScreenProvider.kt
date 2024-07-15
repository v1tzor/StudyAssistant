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

import ru.aleshin.studyassistant.core.common.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.ClassScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.DailyScheduleScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.EmployeeScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.HomeworkScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.OrganizationScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.ProfileScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.WeekScheduleScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.SubjectScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.TodoScreen

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface EditorScreenProvider : FeatureScreenProvider<EditorScreen> {

    class Base : EditorScreenProvider {

        override fun provideFeatureScreen(screen: EditorScreen) = when (screen) {
            is EditorScreen.WeekSchedule -> WeekScheduleScreen(
                week = screen.week,
            )
            is EditorScreen.DailySchedule -> DailyScheduleScreen(
                date = screen.date,
                baseScheduleId = screen.baseScheduleId,
                customScheduleId = screen.customScheduleId,
            )
            is EditorScreen.Class -> ClassScreen(
                classId = screen.classId,
                scheduleId = screen.scheduleId,
                organizationId = screen.organizationId,
                customSchedule = screen.isCustomSchedule,
                weekDay = screen.weekDay,
            )
            is EditorScreen.Subject -> SubjectScreen(
                subjectId = screen.subjectId,
                organizationId = screen.organizationId,
            )
            is EditorScreen.Employee -> EmployeeScreen(
                employeeId = screen.employeeId,
                organizationId = screen.organizationId,
            )
            is EditorScreen.Homework -> HomeworkScreen(
                homeworkId = screen.homeworkId,
                date = screen.date,
                subjectId = screen.subjectId,
                organizationId = screen.organizationId,
            )
            is EditorScreen.Organization -> OrganizationScreen(
                organizationId = screen.organizationId,
            )
            is EditorScreen.Profile -> ProfileScreen()
            is EditorScreen.Todo -> TodoScreen()
        }
    }
}