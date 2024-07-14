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

package ru.aleshin.studyassistant.users.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter
import ru.aleshin.studyassistant.users.impl.navigation.UsersFeatureStarterImpl
import ru.aleshin.studyassistant.users.impl.navigation.UsersScreenProvider
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileEffectCommunicator
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileStateCommunicator
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileWorkProcessor
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.screenmodel.FriendsEffectCommunicator
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.screenmodel.FriendsScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.screenmodel.FriendsStateCommunicator
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.screenmodel.FriendsWorkProcessor
import ru.aleshin.studyassistant.users.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.users.impl.presentation.ui.navigation.NavigationScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.screenmodel.RequestsEffectCommunicator
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.screenmodel.RequestsScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.screenmodel.RequestsStateCommunicator
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.screenmodel.RequestsWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<UsersFeatureStarter> { UsersFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<UsersScreenProvider> { UsersScreenProvider.Base(instance<() -> EditorFeatureStarter>()) }

    bindProvider<EmployeeProfileStateCommunicator> { EmployeeProfileStateCommunicator.Base() }
    bindProvider<EmployeeProfileEffectCommunicator> { EmployeeProfileEffectCommunicator.Base() }
    bindProvider<EmployeeProfileWorkProcessor> { EmployeeProfileWorkProcessor.Base(instance()) }
    bindProvider<EmployeeProfileScreenModel> { EmployeeProfileScreenModel(instance(), instance(), instance(), instance(), instance()) }

    bindProvider<FriendsStateCommunicator> { FriendsStateCommunicator.Base() }
    bindProvider<FriendsEffectCommunicator> { FriendsEffectCommunicator.Base() }
    bindProvider<FriendsWorkProcessor> { FriendsWorkProcessor.Base(instance(), instance()) }
    bindProvider<FriendsScreenModel> { FriendsScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<RequestsStateCommunicator> { RequestsStateCommunicator.Base() }
    bindProvider<RequestsEffectCommunicator> { RequestsEffectCommunicator.Base() }
    bindProvider<RequestsWorkProcessor> { RequestsWorkProcessor.Base(instance(), instance()) }
    bindProvider<RequestsScreenModel> { RequestsScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }
}