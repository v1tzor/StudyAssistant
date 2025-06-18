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
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsFeatureStarterImpl
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsScreenProvider
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.screenmodel.CalendarEffectCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.screenmodel.CalendarScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.screenmodel.CalendarStateCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.screenmodel.CalendarWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.screenmodel.GeneralEffectCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.screenmodel.GeneralScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.screenmodel.GeneralStateCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.screenmodel.GeneralWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.TabNavigationScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.TabNavigationEffectCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.TabNavigationScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.TabNavigationStateCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.screenmodel.NotificationEffectCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.screenmodel.NotificationScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.screenmodel.NotificationStateCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.screenmodel.NotificationWorkProcessor
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.screenmodel.SubscriptionEffectCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.screenmodel.SubscriptionScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.screenmodel.SubscriptionStateCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.screenmodel.SubscriptionWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<TabNavigationScreen> { TabNavigationScreen() }

    bindProvider<SettingsFeatureStarter> { SettingsFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<SettingsScreenProvider> { SettingsScreenProvider.Base(instance<() -> BillingFeatureStarter>()) }

    bindProvider<TabNavigationStateCommunicator> { TabNavigationStateCommunicator.Base() }
    bindProvider<TabNavigationEffectCommunicator> { TabNavigationEffectCommunicator.Base() }
    bindProvider<TabNavigationScreenModel> { TabNavigationScreenModel(instance(), instance(), instance(), instance()) }

    bindSingleton<GeneralStateCommunicator> { GeneralStateCommunicator.Base() }
    bindSingleton<GeneralEffectCommunicator> { GeneralEffectCommunicator.Base() }
    bindSingleton<GeneralWorkProcessor> { GeneralWorkProcessor.Base(instance()) }
    bindSingleton<GeneralScreenModel> { GeneralScreenModel(instance(), instance(), instance(), instance()) }

    bindSingleton<CalendarStateCommunicator> { CalendarStateCommunicator.Base() }
    bindSingleton<CalendarEffectCommunicator> { CalendarEffectCommunicator.Base() }
    bindSingleton<CalendarWorkProcessor> { CalendarWorkProcessor.Base(instance(), instance()) }
    bindSingleton<CalendarScreenModel> { CalendarScreenModel(instance(), instance(), instance(), instance()) }

    bindSingleton<NotificationStateCommunicator> { NotificationStateCommunicator.Base() }
    bindSingleton<NotificationEffectCommunicator> { NotificationEffectCommunicator.Base() }
    bindSingleton<NotificationWorkProcessor> { NotificationWorkProcessor.Base(instance(), instance()) }
    bindSingleton<NotificationScreenModel> { NotificationScreenModel(instance(), instance(), instance(), instance()) }

    bindSingleton<SubscriptionStateCommunicator> { SubscriptionStateCommunicator.Base() }
    bindSingleton<SubscriptionEffectCommunicator> { SubscriptionEffectCommunicator.Base() }
    bindSingleton<SubscriptionWorkProcessor> { SubscriptionWorkProcessor.Base(instance(), instance()) }
    bindSingleton<SubscriptionScreenModel> { SubscriptionScreenModel(instance(), instance(), instance(), instance(), instance()) }
}