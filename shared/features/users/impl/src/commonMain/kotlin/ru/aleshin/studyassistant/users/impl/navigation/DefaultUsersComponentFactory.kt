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

package ru.aleshin.studyassistant.users.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent
import ru.aleshin.studyassistant.users.api.UsersFeatureComponentFactory
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.store.FriendsComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.store.RequestsComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.root.InternalUsersFeatureComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.store.UserProfileComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultUsersComponentFactory(
    private val friendsStoreFactory: FriendsComposeStore.Factory,
    private val requestsStoreFactory: RequestsComposeStore.Factory,
    private val userProfileStoreFactory: UserProfileComposeStore.Factory,
    private val employeeProfileStoreFactory: EmployeeProfileComposeStore.Factory,
) : UsersFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<UsersFeatureComponent.UsersConfig>,
        outputConsumer: OutputConsumer<UsersFeatureComponent.UsersOutput>
    ): UsersFeatureComponent {
        return InternalUsersFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            friendsStoreFactory = friendsStoreFactory,
            requestsStoreFactory = requestsStoreFactory,
            userProfileStoreFactory = userProfileStoreFactory,
            employeeProfileStoreFactory = employeeProfileStoreFactory,
        )
    }
}