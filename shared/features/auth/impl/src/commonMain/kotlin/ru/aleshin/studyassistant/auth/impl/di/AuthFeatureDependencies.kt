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

package ru.aleshin.studyassistant.auth.impl.di

import ru.aleshin.studyassistant.core.api.auth.AccountService
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.AppService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.sync.SourceSyncFacade
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubscriptionsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
public interface AuthFeatureDependencies : BaseFeatureDependencies {
    public val authRepository: AuthRepository
    public val subscriptionsRepository: SubscriptionsRepository
    public val messageRepository: MessageRepository
    public val usersRepository: UsersRepository
    public val generalSettingsRepository: GeneralSettingsRepository
    public val manageUserRepository: ManageUserRepository
    public val deviceInfoProvider: DeviceInfoProvider
    public val accountService: AccountService
    public val coroutineManager: CoroutineManager
    public val sourceSyncFacade: SourceSyncFacade
    public val appService: AppService
    public val crashlyticsService: CrashlyticsService
    public val dateManager: DateManager
}