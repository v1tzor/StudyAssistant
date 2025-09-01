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

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureManager
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.users.api.UsersFeatureApi
import ru.aleshin.studyassistant.users.api.UsersFeatureComponentFactory
import ru.aleshin.studyassistant.users.impl.di.UsersFeatureDependencies
import ru.aleshin.studyassistant.users.impl.di.modules.domainModule
import ru.aleshin.studyassistant.users.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
public object UsersFeatureManager : BaseFeatureManager<UsersFeatureApi, UsersFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun createOrGetFeature(dependencies: UsersFeatureDependencies): UsersFeatureApi {
        val diGraph = directDi

        return if (diGraph != null) {
            diGraph.instance<UsersFeatureApi>()
        } else {
            val di = DI {
                importAll(presentationModule, domainModule)

                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<FriendRequestsRepository> { dependencies.friendRequestsRepository }
                bindSingleton<ShareSchedulesRepository> { dependencies.shareSchedulesRepository }
                bindSingleton<ShareHomeworksRepository> { dependencies.shareHomeworksRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }

                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<Konnection> { dependencies.connectionManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }

                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

                bindProvider<UsersFeatureApi> {
                    object : UsersFeatureApi {
                        override fun componentFactory(): UsersFeatureComponentFactory = instance()
                    }
                }
            }
            directDi = di.direct

            di.direct.instance<UsersFeatureApi>()
        }
    }

    override fun finish() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Users feature DI is not initialized"
    }
}