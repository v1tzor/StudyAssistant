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
import ru.aleshin.studyassistant.auth.api.AuthFeatureStarter
import ru.aleshin.studyassistant.billing.api.BillingFeatureStarter
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.editor.api.EditorFeatureStarter
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponentFactory
import ru.aleshin.studyassistant.preview.api.PreviewFeatureStarter
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureStarter
import ru.aleshin.studyassistant.settings.api.SettingsFeatureStarter
import ru.aleshin.studyassistant.users.api.UsersFeatureStarter

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
        private val previewFeatureStarter: PreviewFeatureStarter,
        private val authFeatureStarter: AuthFeatureStarter,
        private val scheduleFeatureStarter: ScheduleFeatureStarter,
        private val editorFeatureStarter: EditorFeatureStarter,
        private val billingFeatureStarter: BillingFeatureStarter,
        private val settingsFeatureStarter: SettingsFeatureStarter,
        private val usersFeatureStarter: UsersFeatureStarter,
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
                previewFeatureStarter = previewFeatureStarter,
                authFeatureStarter = authFeatureStarter,
                scheduleFeatureStarter = scheduleFeatureStarter,
                editorFeatureStarter = editorFeatureStarter,
                billingFeatureStarter = billingFeatureStarter,
                settingsFeatureStarter = settingsFeatureStarter,
                usersFeatureStarter = usersFeatureStarter,
                tabsComponentFactory = tabsComponentFactory,
            )
        }
    }
}