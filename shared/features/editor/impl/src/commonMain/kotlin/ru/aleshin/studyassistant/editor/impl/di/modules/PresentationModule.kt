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

package ru.aleshin.studyassistant.editor.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponentFactory
import ru.aleshin.studyassistant.editor.impl.navigation.DefaultEditorComponentFactory
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store.ClassComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store.ClassWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.store.DailyScheduleComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.store.DailyScheduleWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store.EmployeeComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store.EmployeeWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store.HomeworkComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store.HomeworkWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store.OrganizationComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store.OrganizationWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store.ProfileComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store.ProfileWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store.WeekScheduleComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store.WeekScheduleWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store.SubjectComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store.SubjectWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoWorkProcessor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<EditorFeatureComponentFactory> { DefaultEditorComponentFactory(instance(), instance(), instance(), instance(),instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<WeekScheduleWorkProcessor> { WeekScheduleWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<WeekScheduleComposeStore.Factory> { WeekScheduleComposeStore.Factory(instance(), instance()) }

    bindSingleton<DailyScheduleWorkProcessor> { DailyScheduleWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<DailyScheduleComposeStore.Factory> { DailyScheduleComposeStore.Factory(instance(), instance()) }

    bindSingleton<ClassWorkProcessor> { ClassWorkProcessor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<ClassComposeStore.Factory> { ClassComposeStore.Factory(instance(), instance()) }

    bindSingleton<SubjectWorkProcessor> { SubjectWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<SubjectComposeStore.Factory> { SubjectComposeStore.Factory(instance(), instance()) }

    bindSingleton<EmployeeWorkProcessor> { EmployeeWorkProcessor.Base(instance(), instance()) }
    bindSingleton<EmployeeComposeStore.Factory> { EmployeeComposeStore.Factory(instance(), instance()) }

    bindSingleton<HomeworkWorkProcessor> { HomeworkWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworkComposeStore.Factory> { HomeworkComposeStore.Factory(instance(), instance(), instance()) }

    bindSingleton<TodoWorkProcessor> { TodoWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<TodoComposeStore.Factory> { TodoComposeStore.Factory(instance(), instance()) }

    bindSingleton<OrganizationWorkProcessor> { OrganizationWorkProcessor.Base(instance()) }
    bindSingleton<OrganizationComposeStore.Factory> { OrganizationComposeStore.Factory(instance(), instance()) }

    bindSingleton<ProfileWorkProcessor> { ProfileWorkProcessor.Base(instance()) }
    bindSingleton<ProfileComposeStore.Factory> { ProfileComposeStore.Factory(instance(), instance()) }
}