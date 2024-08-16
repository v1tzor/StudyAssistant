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

package ru.aleshin.studyassistant.profile.impl.di

import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
interface ProfileFeatureDependencies : BaseFeatureDependencies {
    val authFeatureStarter: () -> AuthFeatureStarter
    val usersFeatureStarter: () -> UsersFeatureStarter
    val settingsFeatureStarter: () -> SettingsFeatureStarter
    val editorFeatureStarter: () -> EditorFeatureStarter
    val scheduleFeatureStarter: () -> ScheduleFeatureStarter
    val authRepository: AuthRepository
    val usersRepository: UsersRepository
    val friendRequestsRepository: FriendRequestsRepository
    val baseSchedulesRepository: BaseScheduleRepository
    val shareSchedulesRepository: ShareSchedulesRepository
    val organizationsRepository: OrganizationsRepository
    val messageRepository: MessageRepository
    val deviceInfoProvider: DeviceInfoProvider
    val coroutineManager: CoroutineManager
    val dateManager: DateManager
}