/*
 * Copyright 2026 Stanislav Aleshin
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
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponentDeps
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.tasks.api.TasksConfig
import ru.aleshin.studyassistant.tasks.api.TasksContentProviderFactory
import ru.aleshin.studyassistant.tasks.api.TasksOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.root.TasksContentProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.root.TasksFeatureComponent

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
internal class DefaultTasksContentProviderFactory(
    private val di: DI,
) : TasksContentProviderFactory {

    override fun createProvider(
        componentContext: ComponentContext,
        startConfig: TasksConfig,
        outputConsumer: OutputConsumer<TasksOutput>,
    ): FeatureContentProvider {
        val deps = TasksComponentDeps(startConfig, outputConsumer)
        val component by di.on(componentContext).instance<TasksComponentDeps, TasksFeatureComponent>(arg = deps)
        return TasksContentProvider(di, component)
    }
}

internal typealias TasksComponentDeps = FeatureComponentDeps<TasksConfig, TasksOutput>
