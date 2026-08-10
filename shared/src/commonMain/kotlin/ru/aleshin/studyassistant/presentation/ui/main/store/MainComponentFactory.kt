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

package ru.aleshin.studyassistant.presentation.ui.main.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.analytics.api.AnalyticsDecomposeFeatureFactory
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.editor.api.EditorDecomposeFeatureFactory
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponentFactory
import ru.aleshin.studyassistant.preview.api.PreviewDecomposeFeatureFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleDecomposeFeatureFactory
import ru.aleshin.studyassistant.settings.api.SettingsDecomposeFeatureFactory
import ru.aleshin.studyassistant.users.api.UsersDecomposeFeatureFactory

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
interface MainComponentFactory {

    fun createComponent(
        componentContext: ComponentContext,
        deepLink: DeepLinkUrl? = null,
    ): MainComponent

    class Default(
        private val storeFactory: MainComposeStore.Factory,
        private val previewFeatureFactory: PreviewDecomposeFeatureFactory,
        private val scheduleFeatureFactory: ScheduleDecomposeFeatureFactory,
        private val editorFeatureFactory: EditorDecomposeFeatureFactory,
        private val settingsFeatureFactory: SettingsDecomposeFeatureFactory,
        private val analyticsFeatureFactory: AnalyticsDecomposeFeatureFactory,
        private val usersFeatureFactory: UsersDecomposeFeatureFactory,
        private val tabsComponentFactory: TabsComponentFactory,
    ) : MainComponentFactory {

        override fun createComponent(
            componentContext: ComponentContext,
            deepLink: DeepLinkUrl?,
        ): MainComponent {
            return MainComponent.Default(
                storeFactory = storeFactory,
                componentContext = componentContext,
                deepLink = deepLink,
                previewFeatureFactory = previewFeatureFactory,
                scheduleFeatureFactory = scheduleFeatureFactory,
                editorFeatureFactory = editorFeatureFactory,
                settingsFeatureFactory = settingsFeatureFactory,
                analyticsFeatureFactory = analyticsFeatureFactory,
                usersFeatureFactory = usersFeatureFactory,
                tabsComponentFactory = tabsComponentFactory,
            )
        }
    }
}
