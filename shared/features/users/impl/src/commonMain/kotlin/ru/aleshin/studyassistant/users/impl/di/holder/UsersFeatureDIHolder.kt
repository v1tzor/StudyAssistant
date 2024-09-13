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

package ru.aleshin.studyassistant.users.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.inject.CrashlyticsService
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.users.api.di.UsersFeatureApi
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter
import ru.aleshin.studyassistant.users.impl.di.UsersFeatureDependencies
import ru.aleshin.studyassistant.users.impl.di.modules.domainModule
import ru.aleshin.studyassistant.users.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.users.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
public object UsersFeatureDIHolder : BaseFeatureDIHolder<UsersFeatureApi, UsersFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: UsersFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> EditorFeatureStarter> { dependencies.editorFeatureStarter }
                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<FriendRequestsRepository> { dependencies.friendRequestsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<UsersFeatureApi> {
                    object : UsersFeatureApi {
                        override fun fetchStarter() = instance<UsersFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): UsersFeatureApi {
        return fetchDI().instance<UsersFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Users feature DI is not initialized"
    }
}