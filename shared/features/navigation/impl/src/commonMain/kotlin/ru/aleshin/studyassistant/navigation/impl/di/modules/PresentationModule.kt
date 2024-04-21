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

package ru.aleshin.studyassistant.navigation.impl.di.modules

import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.navigation.impl.navigation.NavigationFeatureStarterImpl
import ru.aleshin.studyassistant.navigation.impl.navigation.NavigationScreenProvider
import ru.aleshin.studyassistant.navigation.impl.ui.TabsScreen
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.TabsEffectCommunicator
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.TabsScreenModel
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.TabsStateCommunicator
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<Screen> { TabsScreen() }

    bindSingleton<NavigationFeatureStarter> { NavigationFeatureStarterImpl(instance()) }
    bindSingleton<NavigationScreenProvider> { NavigationScreenProvider.Base(instance<() -> ScheduleFeatureStarter>()) }

    bindSingleton<TabsEffectCommunicator> { TabsEffectCommunicator.Base() }
    bindSingleton<TabsStateCommunicator> { TabsStateCommunicator.Base() }
    bindSingleton<TabsScreenModel> { TabsScreenModel(instance(), instance(), instance(), instance()) }
}