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

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.profile.api.di.ProfileFeatureApi
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.profile.impl.di.ProfileFeatureDependencies
import ru.aleshin.studyassistant.profile.impl.di.modules.domainModule
import ru.aleshin.studyassistant.profile.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
public object ProfileFeatureDIHolder : BaseFeatureDIHolder<ProfileFeatureApi, ProfileFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: ProfileFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(presentationModule, domainModule)
                bindSingleton<() -> AuthFeatureStarter> { dependencies.authFeatureStarter }
                bindSingleton<() -> UsersFeatureStarter> { dependencies.usersFeatureStarter }
                bindSingleton<() -> SettingsFeatureStarter> { dependencies.settingsFeatureStarter }
                bindSingleton<() -> EditorFeatureStarter> { dependencies.editorFeatureStarter }
                bindSingleton<() -> ScheduleFeatureStarter> { dependencies.scheduleFeatureStarter }
                bindSingleton<AuthRepository> { dependencies.authRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<FriendRequestsRepository> { dependencies.friendRequestsRepository }
                bindSingleton<ShareSchedulesRepository> { dependencies.shareSchedulesRepository }
                bindSingleton<BaseScheduleRepository> { dependencies.baseSchedulesRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<StartClassesReminderManager> { dependencies.startClassesReminderManager }
                bindSingleton<EndClassesReminderManager> { dependencies.endClassesReminderManager }
                bindSingleton<WorkloadWarningManager> { dependencies.workloadWarningManager }
                bindSingleton<HomeworksReminderManager> { dependencies.homeworksReminderManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<ProfileFeatureApi> {
                    object : ProfileFeatureApi {
                        override fun fetchStarter() = instance<ProfileFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): ProfileFeatureApi {
        return fetchDI().instance<ProfileFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Profile feature DI is not initialized"
    }
}