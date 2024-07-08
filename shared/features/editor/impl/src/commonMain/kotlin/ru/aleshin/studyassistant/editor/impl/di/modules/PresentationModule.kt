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
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassEditorEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassEditorStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel.ClassEditorWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeEditorEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeEditorStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.EmployeeEditorWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkEditorEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkEditorStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.screenmodel.HomeworkEditorWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.editor.impl.presentation.ui.navigation.NavigationScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationEditorEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationEditorStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.OrganizationEditorWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.ScheduleEditorEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.ScheduleEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.ScheduleEditorStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel.ScheduleEditorWorkProcessor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectEditorEffectCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectEditorStateCommunicator
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.screeenmodel.SubjectEditorWorkProcessor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<EditorFeatureStarter> { EditorFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<EditorScreenProvider> { EditorScreenProvider.Base() }

    bindProvider<ScheduleEditorStateCommunicator> { ScheduleEditorStateCommunicator.Base() }
    bindProvider<ScheduleEditorEffectCommunicator> { ScheduleEditorEffectCommunicator.Base() }
    bindProvider<ScheduleEditorWorkProcessor> { ScheduleEditorWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ScheduleEditorScreenModel> { ScheduleEditorScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<ClassEditorStateCommunicator> { ClassEditorStateCommunicator.Base() }
    bindProvider<ClassEditorEffectCommunicator> { ClassEditorEffectCommunicator.Base() }
    bindProvider<ClassEditorWorkProcessor> { ClassEditorWorkProcessor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ClassEditorScreenModel> { ClassEditorScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<SubjectEditorStateCommunicator> { SubjectEditorStateCommunicator.Base() }
    bindProvider<SubjectEditorEffectCommunicator> { SubjectEditorEffectCommunicator.Base() }
    bindProvider<SubjectEditorWorkProcessor> { SubjectEditorWorkProcessor.Base(instance(), instance(), instance()) }
    bindProvider<SubjectEditorScreenModel> { SubjectEditorScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<EmployeeEditorStateCommunicator> { EmployeeEditorStateCommunicator.Base() }
    bindProvider<EmployeeEditorEffectCommunicator> { EmployeeEditorEffectCommunicator.Base() }
    bindProvider<EmployeeEditorWorkProcessor> { EmployeeEditorWorkProcessor.Base(instance(), instance()) }
    bindProvider<EmployeeEditorScreenModel> { EmployeeEditorScreenModel(instance(), instance(), instance(), instance()) }

    bindProvider<HomeworkEditorStateCommunicator> { HomeworkEditorStateCommunicator.Base() }
    bindProvider<HomeworkEditorEffectCommunicator> { HomeworkEditorEffectCommunicator.Base() }
    bindProvider<HomeworkEditorWorkProcessor> { HomeworkEditorWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindProvider<HomeworkEditorScreenModel> { HomeworkEditorScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<OrganizationEditorStateCommunicator> { OrganizationEditorStateCommunicator.Base() }
    bindProvider<OrganizationEditorEffectCommunicator> { OrganizationEditorEffectCommunicator.Base() }
    bindProvider<OrganizationEditorWorkProcessor> { OrganizationEditorWorkProcessor.Base(instance()) }
    bindProvider<OrganizationEditorScreenModel> { OrganizationEditorScreenModel(instance(), instance(), instance(), instance()) }
}