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

package ru.aleshin.studyassistant.settings.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponentFactory
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.InternalSettingsFeatureComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.TabNavigationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store.SubscriptionComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultSettingsComponentFactory(
    private val tabNavigationStoreFactory: TabNavigationComposeStore.Factory,
    private val generalStoreFactory: GeneralComposeStore.Factory,
    private val notificationStoreFactory: NotificationComposeStore.Factory,
    private val calendarStoreFactory: CalendarComposeStore.Factory,
    private val subscriptionStoreFactory: SubscriptionComposeStore.Factory,
    private val aboutAppStoreFactory: AboutAppComposeStore.Factory,
) : SettingsFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<SettingsFeatureComponent.SettingsConfig>,
        outputConsumer: OutputConsumer<SettingsFeatureComponent.SettingsOutput>
    ): SettingsFeatureComponent {
        return InternalSettingsFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            tabNavigationStoreFactory = tabNavigationStoreFactory,
            generalStoreFactory = generalStoreFactory,
            notificationStoreFactory = notificationStoreFactory,
            calendarStoreFactory = calendarStoreFactory,
            subscriptionStoreFactory = subscriptionStoreFactory,
            aboutAppStoreFactory = aboutAppStoreFactory,
        )
    }
}