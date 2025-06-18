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

package ru.aleshin.studyassistant.billing.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.billing.impl.navigation.BillingFeatureStarterImpl
import ru.aleshin.studyassistant.billing.impl.navigation.BillingScreenProvider
import ru.aleshin.studyassistant.billing.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.billing.impl.presentation.ui.navigation.NavigationScreenModel
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel.SubscriptionEffectCommunicator
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel.SubscriptionScreenModel
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel.SubscriptionStateCommunicator
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel.SubscriptionWorkProcessor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<BillingFeatureStarter> { BillingFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<BillingScreenProvider> { BillingScreenProvider.Base() }

    bindProvider<SubscriptionStateCommunicator> { SubscriptionStateCommunicator.Base() }
    bindProvider<SubscriptionEffectCommunicator> { SubscriptionEffectCommunicator.Base() }
    bindProvider<SubscriptionWorkProcessor> { SubscriptionWorkProcessor.Base(instance()) }
    bindProvider<SubscriptionScreenModel> { SubscriptionScreenModel(instance(), instance(), instance(), instance()) }
}