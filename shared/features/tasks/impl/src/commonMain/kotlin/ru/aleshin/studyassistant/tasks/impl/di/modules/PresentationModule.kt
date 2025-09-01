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
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponentFactory
import ru.aleshin.studyassistant.tasks.impl.navigation.DefaultTasksComponentFactory
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store.HomeworksComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store.HomeworksDetailsWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.GoalWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.HomeworksWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.OverviewComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.TodoWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareWorkProcessor
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store.TodoComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store.TodoDetailsWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<TasksFeatureComponentFactory> { DefaultTasksComponentFactory(instance(), instance(), instance(), instance()) }

    bindSingleton<HomeworksWorkProcessor> { HomeworksWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<TodoWorkProcessor> { TodoWorkProcessor.Base(instance()) }
    bindSingleton<GoalWorkProcessor> { GoalWorkProcessor.Base(instance(), instance()) }
    bindSingleton<OverviewComposeStore.Factory> { OverviewComposeStore.Factory(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<HomeworksDetailsWorkProcessor> { HomeworksDetailsWorkProcessor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworksComposeStore.Factory> { HomeworksComposeStore.Factory(instance(), instance(), instance()) }

    bindSingleton<ShareWorkProcessor> { ShareWorkProcessor.Base(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<ShareComposeStore.Factory> { ShareComposeStore.Factory(instance(), instance()) }

    bindSingleton<TodoDetailsWorkProcessor> { TodoDetailsWorkProcessor.Base(instance()) }
    bindSingleton<TodoComposeStore.Factory> { TodoComposeStore.Factory(instance(), instance()) }
}