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

package ru.aleshin.studyassistant.settings.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponentFactory
import ru.aleshin.studyassistant.settings.impl.navigation.DefaultSettingsComponentFactory
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.TabNavigationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store.SubscriptionComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store.SubscriptionWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<SettingsFeatureComponentFactory> { DefaultSettingsComponentFactory(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<TabNavigationComposeStore.Factory> { TabNavigationComposeStore.Factory(instance()) }

    bindSingleton<GeneralWorkProcessor> { GeneralWorkProcessor.Base(instance()) }
    bindSingleton<GeneralComposeStore.Factory> { GeneralComposeStore.Factory(instance(), instance()) }

    bindSingleton<CalendarWorkProcessor> { CalendarWorkProcessor.Base(instance(), instance()) }
    bindSingleton<CalendarComposeStore.Factory> { CalendarComposeStore.Factory(instance(), instance()) }

    bindSingleton<NotificationWorkProcessor> { NotificationWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<NotificationComposeStore.Factory> { NotificationComposeStore.Factory(instance(), instance()) }

    bindSingleton<SubscriptionWorkProcessor> { SubscriptionWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<SubscriptionComposeStore.Factory> { SubscriptionComposeStore.Factory(instance(), instance()) }

    bindSingleton<AboutAppComposeStore.Factory> { AboutAppComposeStore.Factory(instance()) }
}