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

package ru.aleshin.studyassistant.tasks.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksConfig
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksOutput
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponentFactory
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store.HomeworksComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.OverviewComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.root.InternalTasksFeatureComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store.TodoComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultTasksComponentFactory(
    private val overviewStoreFactory: OverviewComposeStore.Factory,
    private val todoStoreFactory: TodoComposeStore.Factory,
    private val homeworksStoreFactory: HomeworksComposeStore.Factory,
    private val shareStoreFactory: ShareComposeStore.Factory,
) : TasksFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<TasksConfig>,
        outputConsumer: OutputConsumer<TasksOutput>
    ): TasksFeatureComponent {
        return InternalTasksFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            overviewStoreFactory = overviewStoreFactory,
            todoStoreFactory = todoStoreFactory,
            homeworksStoreFactory = homeworksStoreFactory,
            shareStoreFactory = shareStoreFactory,
        )
    }
}