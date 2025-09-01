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
import ru.aleshin.studyassistant.chat.api.ChatFeatureStarter
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureStarter
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsConfig
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsOutput
import ru.aleshin.studyassistant.profile.api.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureStarter
import ru.aleshin.studyassistant.tasks.api.TasksFeatureStarter

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
interface TabsComponentFactory {

    fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<TabsConfig>,
        outputConsumer: OutputConsumer<TabsOutput>
    ): TabsComponent

    class Default(
        private val scheduleFeatureStarter: ScheduleFeatureStarter,
        private val tasksFeatureStarter: TasksFeatureStarter,
        private val chatFeatureStarter: ChatFeatureStarter,
        private val infoFeatureStarter: InfoFeatureStarter,
        private val profileFeatureStarter: ProfileFeatureStarter,
    ) : TabsComponentFactory {

        override fun createComponent(
            componentContext: ComponentContext,
            startConfig: StartFeatureConfig<TabsConfig>,
            outputConsumer: OutputConsumer<TabsOutput>
        ): TabsComponent {
            return TabsComponent.Default(
                componentContext = componentContext,
                startConfig = startConfig,
                outputConsumer = outputConsumer,
                scheduleFeatureStarter = scheduleFeatureStarter,
                tasksFeatureStarter = tasksFeatureStarter,
                chatFeatureStarter = chatFeatureStarter,
                infoFeatureStarter = infoFeatureStarter,
                profileFeatureStarter = profileFeatureStarter,
            )
        }
    }
}