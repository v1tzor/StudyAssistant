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

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorOutput
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponentFactory
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store.ClassComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.store.DailyScheduleComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store.EmployeeComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store.HomeworkComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store.OrganizationComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store.ProfileComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.root.InternalEditorFeatureComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store.WeekScheduleComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store.SubjectComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultEditorComponentFactory(
    private val classStoreFactory: ClassComposeStore.Factory,
    private val employeeStoreFactory: EmployeeComposeStore.Factory,
    private val weekScheduleStoreFactory: WeekScheduleComposeStore.Factory,
    private val dailyScheduleStoreFactory: DailyScheduleComposeStore.Factory,
    private val subjectStoreFactory: SubjectComposeStore.Factory,
    private val homeworkStoreFactory: HomeworkComposeStore.Factory,
    private val todoStoreFactory: TodoComposeStore.Factory,
    private val organizationStoreFactory: OrganizationComposeStore.Factory,
    private val profileStoreFactory: ProfileComposeStore.Factory
) : EditorFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<EditorConfig>,
        outputConsumer: OutputConsumer<EditorOutput>
    ): EditorFeatureComponent {
        return InternalEditorFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            classStoreFactory = classStoreFactory,
            employeeStoreFactory = employeeStoreFactory,
            weekScheduleStoreFactory = weekScheduleStoreFactory,
            dailyScheduleStoreFactory = dailyScheduleStoreFactory,
            subjectStoreFactory = subjectStoreFactory,
            homeworkStoreFactory = homeworkStoreFactory,
            todoStoreFactory = todoStoreFactory,
            organizationStoreFactory = organizationStoreFactory,
            profileStoreFactory = profileStoreFactory,
        )
    }
}