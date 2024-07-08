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
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsFeatureStarterImpl
import ru.aleshin.studyassistant.settings.impl.navigation.SettingsScreenProvider
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.TabNavigationScreen
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.TabNavigationEffectCommunicator
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.TabNavigationScreenModel
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.screenmodel.TabNavigationStateCommunicator

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<TabNavigationScreen> { TabNavigationScreen() }

    bindProvider<SettingsFeatureStarter> { SettingsFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<SettingsScreenProvider> { SettingsScreenProvider.Base() }

    bindProvider<TabNavigationStateCommunicator> { TabNavigationStateCommunicator.Base() }
    bindProvider<TabNavigationEffectCommunicator> { TabNavigationEffectCommunicator.Base() }
    bindProvider<TabNavigationScreenModel> { TabNavigationScreenModel(instance(), instance(), instance(), instance()) }
}