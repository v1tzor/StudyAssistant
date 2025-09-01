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

package ru.aleshin.studyassistant.profile.impl.di.holder

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureManager
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.managers.sync.SourceSyncFacade
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.profile.api.ProfileFeatureApi
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponentFactory
import ru.aleshin.studyassistant.profile.impl.di.ProfileFeatureDependencies
import ru.aleshin.studyassistant.profile.impl.di.modules.domainModule
import ru.aleshin.studyassistant.profile.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
public object ProfileFeatureManager : BaseFeatureManager<ProfileFeatureApi, ProfileFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun createOrGetFeature(dependencies: ProfileFeatureDependencies): ProfileFeatureApi {
        val diGraph = directDi

        if (diGraph != null) {
            return diGraph.instance<ProfileFeatureApi>()
        } else {
            val di = DI {
                importAll(presentationModule, domainModule)

                bindSingleton<AuthRepository> { dependencies.authRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<FriendRequestsRepository> { dependencies.friendRequestsRepository }
                bindSingleton<ShareSchedulesRepository> { dependencies.shareSchedulesRepository }
                bindSingleton<BaseScheduleRepository> { dependencies.baseSchedulesRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }

                bindSingleton<SourceSyncFacade> { dependencies.sourceSyncFacade }

                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<StartClassesReminderManager> { dependencies.startClassesReminderManager }
                bindSingleton<EndClassesReminderManager> { dependencies.endClassesReminderManager }
                bindSingleton<WorkloadWarningManager> { dependencies.workloadWarningManager }
                bindSingleton<HomeworksReminderManager> { dependencies.homeworksReminderManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<Konnection> { dependencies.connectionManager }

                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

                bindSingleton<ProfileFeatureApi> {
                    object : ProfileFeatureApi {
                        override fun componentFactory(): ProfileFeatureComponentFactory = instance()
                    }
                }
            }
            directDi = di.direct

            return di.direct.instance<ProfileFeatureApi>()
        }
    }

    override fun finish() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Profile feature DI is not initialized"
    }
}