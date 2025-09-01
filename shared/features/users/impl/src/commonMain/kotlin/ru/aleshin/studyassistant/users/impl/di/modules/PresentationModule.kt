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
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.users.api.UsersFeatureComponentFactory
import ru.aleshin.studyassistant.users.impl.navigation.DefaultUsersComponentFactory
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileWorkProcessor
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.store.FriendsComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.store.FriendsWorkProcessor
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.store.RequestsComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.store.RequestsWorkProcessor
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.store.UserProfileComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.store.UserProfileWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<UsersFeatureComponentFactory> { DefaultUsersComponentFactory(instance(), instance(), instance(), instance()) }

    bindSingleton<EmployeeProfileWorkProcessor> { EmployeeProfileWorkProcessor.Base(instance()) }
    bindSingleton<EmployeeProfileComposeStore.Factory> { EmployeeProfileComposeStore.Factory(instance(), instance()) }

    bindSingleton<UserProfileWorkProcessor> { UserProfileWorkProcessor.Base(instance(), instance()) }
    bindSingleton<UserProfileComposeStore.Factory> { UserProfileComposeStore.Factory(instance(), instance()) }

    bindSingleton<FriendsWorkProcessor> { FriendsWorkProcessor.Base(instance(), instance()) }
    bindSingleton<FriendsComposeStore.Factory> { FriendsComposeStore.Factory(instance(), instance(), instance()) }

    bindSingleton<RequestsWorkProcessor> { RequestsWorkProcessor.Base(instance(), instance()) }
    bindSingleton<RequestsComposeStore.Factory> { RequestsComposeStore.Factory(instance(), instance(), instance()) }
}