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
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.impl.navigation.EditorFeatureStarterImpl
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel.DailyScheduleEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel.DailyScheduleScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel.DailyScheduleStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel.DailyScheduleWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.navigation.NavigationScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.screenmodel.ProfileEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.screenmodel.ProfileScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.screenmodel.ProfileStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.screenmodel.ProfileWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.WeekScheduleEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.WeekScheduleScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.WeekScheduleStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.WeekScheduleWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.TodoWorkProcessor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<EditorFeatureStarter> { EditorFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<EditorScreenProvider> { EditorScreenProvider.Base(instance()) }

    bindProvider<WeekScheduleStateCommunicator> { WeekScheduleStateCommunicator.Base() }
    bindProvider<WeekScheduleEffectCommunicator> { WeekScheduleEffectCommunicator.Base() }
    bindProvider<WeekScheduleWorkProcessor> { WeekScheduleWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindProvider<WeekScheduleScreenModel> { WeekScheduleScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<DailyScheduleStateCommunicator> { DailyScheduleStateCommunicator.Base() }
    bindProvider<DailyScheduleEffectCommunicator> { DailyScheduleEffectCommunicator.Base() }
    bindProvider<DailyScheduleWorkProcessor> { DailyScheduleWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindProvider<DailyScheduleScreenModel> { DailyScheduleScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<ClassStateCommunicator> { ClassStateCommunicator.Base() }
    bindProvider<ClassEffectCommunicator> { ClassEffectCommunicator.Base() }
    bindProvider<ClassWorkProcessor> { ClassWorkProcessor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ClassScreenModel> { ClassScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<SubjectStateCommunicator> { SubjectStateCommunicator.Base() }
    bindProvider<SubjectEffectCommunicator> { SubjectEffectCommunicator.Base() }
    bindProvider<SubjectWorkProcessor> { SubjectWorkProcessor.Base(instance(), instance(), instance()) }
    bindProvider<SubjectScreenModel> { SubjectScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<EmployeeStateCommunicator> { EmployeeStateCommunicator.Base() }
    bindProvider<EmployeeEffectCommunicator> { EmployeeEffectCommunicator.Base() }
    bindProvider<EmployeeWorkProcessor> { EmployeeWorkProcessor.Base(instance(), instance()) }
    bindProvider<EmployeeScreenModel> { EmployeeScreenModel(instance(), instance(), instance(), instance()) }

    bindProvider<HomeworkStateCommunicator> { HomeworkStateCommunicator.Base() }
    bindProvider<HomeworkEffectCommunicator> { HomeworkEffectCommunicator.Base() }
    bindProvider<HomeworkWorkProcessor> { HomeworkWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindProvider<HomeworkScreenModel> { HomeworkScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<TodoStateCommunicator> { TodoStateCommunicator.Base() }
    bindProvider<TodoEffectCommunicator> { TodoEffectCommunicator.Base() }
    bindProvider<TodoWorkProcessor> { TodoWorkProcessor.Base(instance()) }
    bindProvider<TodoScreenModel> { TodoScreenModel(instance(), instance(), instance(), instance()) }

    bindProvider<OrganizationStateCommunicator> { OrganizationStateCommunicator.Base() }
    bindProvider<OrganizationEffectCommunicator> { OrganizationEffectCommunicator.Base() }
    bindProvider<OrganizationWorkProcessor> { OrganizationWorkProcessor.Base(instance()) }
    bindProvider<OrganizationScreenModel> { OrganizationScreenModel(instance(), instance(), instance(), instance()) }

    bindProvider<ProfileStateCommunicator> { ProfileStateCommunicator.Base() }
    bindProvider<ProfileEffectCommunicator> { ProfileEffectCommunicator.Base() }
    bindProvider<ProfileWorkProcessor> { ProfileWorkProcessor.Base(instance()) }
    bindProvider<ProfileScreenModel> { ProfileScreenModel(instance(), instance(), instance(), instance(), instance()) }
}