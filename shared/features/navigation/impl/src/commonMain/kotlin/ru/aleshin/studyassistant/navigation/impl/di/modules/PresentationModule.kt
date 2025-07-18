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

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.chat.api.navigation.ChatFeatureStarter
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.navigation.impl.navigation.NavigationFeatureStarterImpl
import ru.aleshin.studyassistant.navigation.impl.navigation.TabScreenProvider
import ru.aleshin.studyassistant.navigation.impl.ui.TabsScreen
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.TabsEffectCommunicator
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.TabsScreenModel
import ru.aleshin.studyassistant.navigation.impl.ui.screenmodel.TabsStateCommunicator
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<TabsScreen> { TabsScreen() }

    bindSingleton<NavigationFeatureStarter> { NavigationFeatureStarterImpl(instance()) }
    bindSingleton<TabScreenProvider> { TabScreenProvider.Base(instance<() -> ScheduleFeatureStarter>(), instance<() -> TasksFeatureStarter>(), instance<() -> ChatFeatureStarter>(),instance<() -> InfoFeatureStarter>(), instance<() -> ProfileFeatureStarter>()) }

    bindSingleton<TabsEffectCommunicator> { TabsEffectCommunicator.Base() }
    bindSingleton<TabsStateCommunicator> { TabsStateCommunicator.Base() }
    bindSingleton<TabsScreenModel> { TabsScreenModel(instance(), instance(), instance(), instance()) }
}