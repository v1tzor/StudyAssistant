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

package ru.aleshin.studyassistant.settings.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.billing.api.navigation.BillingScreen
import ru.aleshin.studyassistant.core.common.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.settings.api.navigation.SettingsScreen
import ru.aleshin.studyassistant.settings.api.presentation.SettingsRootScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.CalendarScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.GeneralScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.AboutAppScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.NotificationScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.SubscriptionScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface SettingsScreenProvider : FeatureScreenProvider<SettingsScreen, SettingsRootScreen> {

    fun provideBillingScreen(screen: BillingScreen): Screen

    class Base(
        private val billingFeatureStarter: () -> BillingFeatureStarter,
    ) : SettingsScreenProvider {

        override fun provideFeatureScreen(screen: SettingsScreen) = when (screen) {
            is SettingsScreen.General -> GeneralScreen()
            is SettingsScreen.Notification -> NotificationScreen()
            is SettingsScreen.Calendar -> CalendarScreen()
            is SettingsScreen.Subscription -> SubscriptionScreen()
            is SettingsScreen.AboutApp -> AboutAppScreen()
        }

        override fun provideBillingScreen(screen: BillingScreen): Screen {
            return billingFeatureStarter().fetchRootScreenAndNavigate(screen)
        }
    }
}