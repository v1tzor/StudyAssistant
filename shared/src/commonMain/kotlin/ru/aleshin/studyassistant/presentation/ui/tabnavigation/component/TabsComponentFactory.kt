/*
 * Copyright 2025 Stanislav Aleshin
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

package ru.aleshin.studyassistant.presentation.ui.tabnavigation.component

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.chat.api.ChatDecomposeFeatureFactory
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.info.api.InfoDecomposeFeatureFactory
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsConfig
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsOutput
import ru.aleshin.studyassistant.profile.api.ProfileDecomposeFeatureFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleDecomposeFeatureFactory
import ru.aleshin.studyassistant.tasks.api.TasksDecomposeFeatureFactory

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
interface TabsComponentFactory {

    fun createComponent(
        componentContext: ComponentContext,
        startConfig: TabsConfig,
        outputConsumer: OutputConsumer<TabsOutput>
    ): TabsComponent

    class Default(
        private val scheduleFeatureFactory: ScheduleDecomposeFeatureFactory,
        private val tasksFeatureFactory: TasksDecomposeFeatureFactory,
        private val chatFeatureFactory: ChatDecomposeFeatureFactory,
        private val infoFeatureFactory: InfoDecomposeFeatureFactory,
        private val profileFeatureFactory: ProfileDecomposeFeatureFactory,
    ) : TabsComponentFactory {

        override fun createComponent(
            componentContext: ComponentContext,
            startConfig: TabsConfig,
            outputConsumer: OutputConsumer<TabsOutput>
        ): TabsComponent {
            return TabsComponent.Default(
                componentContext = componentContext,
                startConfig = startConfig,
                outputConsumer = outputConsumer,
                scheduleFeatureFactory = scheduleFeatureFactory,
                tasksFeatureFactory = tasksFeatureFactory,
                chatFeatureFactory = chatFeatureFactory,
                infoFeatureFactory = infoFeatureFactory,
                profileFeatureFactory = profileFeatureFactory,
            )
        }
    }
}
