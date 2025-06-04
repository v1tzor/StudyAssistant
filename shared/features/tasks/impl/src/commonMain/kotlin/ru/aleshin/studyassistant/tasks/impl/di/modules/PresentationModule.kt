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

package ru.aleshin.studyassistant.tasks.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksFeatureStarterImpl
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel.HomeworksEffectCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel.HomeworksScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel.HomeworksStateCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel.HomeworksWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.navigation.NavigationScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.OverviewEffectCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.OverviewScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.OverviewStateCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.OverviewWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.screenmodel.ShareEffectCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.screenmodel.ShareScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.screenmodel.ShareStateCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.screenmodel.ShareWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.screenmodel.TodoEffectCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.screenmodel.TodoScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.screenmodel.TodoStateCommunicator
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.screenmodel.TodoWorkProcessor
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<TasksFeatureStarter> { TasksFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<TasksScreenProvider> { TasksScreenProvider.Base(instance<() -> EditorFeatureStarter>(), instance<() -> UsersFeatureStarter>()) }

    bindProvider<OverviewStateCommunicator> { OverviewStateCommunicator.Base() }
    bindProvider<OverviewEffectCommunicator> { OverviewEffectCommunicator.Base() }
    bindProvider<OverviewWorkProcessor> { OverviewWorkProcessor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<OverviewScreenModel> { OverviewScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<HomeworksStateCommunicator> { HomeworksStateCommunicator.Base() }
    bindProvider<HomeworksEffectCommunicator> { HomeworksEffectCommunicator.Base() }
    bindProvider<HomeworksWorkProcessor> { HomeworksWorkProcessor.Base(instance(), instance(), instance()) }
    bindProvider<HomeworksScreenModel> { HomeworksScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<ShareStateCommunicator> { ShareStateCommunicator.Base() }
    bindProvider<ShareEffectCommunicator> { ShareEffectCommunicator.Base() }
    bindProvider<ShareWorkProcessor> { ShareWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ShareScreenModel> { ShareScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<TodoStateCommunicator> { TodoStateCommunicator.Base() }
    bindProvider<TodoEffectCommunicator> { TodoEffectCommunicator.Base() }
    bindProvider<TodoWorkProcessor> { TodoWorkProcessor.Base(instance(), instance()) }
    bindProvider<TodoScreenModel> { TodoScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }
}