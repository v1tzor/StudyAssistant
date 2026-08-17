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

package ru.aleshin.studyassistant.settings.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.multiton
import org.kodein.di.scoped
import ru.aleshin.studyassistant.core.common.di.scope.FeatureComponentScope
import ru.aleshin.studyassistant.settings.api.SettingsContentProviderFactory
import ru.aleshin.studyassistant.settings.impl.navigation.DefaultSettingsContentProviderFactory
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsComponentDeps
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.TabNavigationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.root.SettingsFeatureComponent

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bind<SettingsFeatureComponent>() with scoped(FeatureComponentScope).multiton { deps: SettingsComponentDeps ->
        SettingsFeatureComponent.Default(
            componentContext = context,
            startConfig = deps.startConfig,
            outputConsumer = deps.outputConsumer,
            tabNavigationStoreFactory = instance(),
            generalStoreFactory = instance(),
            notificationStoreFactory = instance(),
            calendarStoreFactory = instance(),
            aiSettingsStoreFactory = instance(),
            aboutAppStoreFactory = instance(),
        )
    }
    bindSingleton<SettingsContentProviderFactory> { DefaultSettingsContentProviderFactory(di) }

    bindSingleton<TabNavigationComposeStore.Factory> { TabNavigationComposeStore.Factory(instance()) }

    bindSingleton<GeneralWorkProcessor> { GeneralWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<GeneralComposeStore.Factory> { GeneralComposeStore.Factory(instance(), instance()) }

    bindSingleton<CalendarWorkProcessor> { CalendarWorkProcessor.Base(instance(), instance()) }
    bindSingleton<CalendarComposeStore.Factory> { CalendarComposeStore.Factory(instance(), instance()) }

    bindSingleton<AiSettingsWorkProcessor> { AiSettingsWorkProcessor.Base(instance()) }
    bindSingleton<AiSettingsComposeStore.Factory> { AiSettingsComposeStore.Factory(instance(), instance()) }

    bindSingleton<NotificationWorkProcessor> { NotificationWorkProcessor.Base(instance(), instance()) }
    bindSingleton<NotificationComposeStore.Factory> { NotificationComposeStore.Factory(instance(), instance()) }

    bindSingleton<AboutAppComposeStore.Factory> { AboutAppComposeStore.Factory(instance()) }
}
