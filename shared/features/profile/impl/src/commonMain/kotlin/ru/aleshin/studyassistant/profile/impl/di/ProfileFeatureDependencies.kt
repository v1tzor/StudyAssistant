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

import dev.tmapps.konnection.Konnection
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
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
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
public interface ProfileFeatureDependencies : BaseFeatureDependencies {
    public val authFeatureStarter: () -> AuthFeatureStarter
    public val usersFeatureStarter: () -> UsersFeatureStarter
    public val settingsFeatureStarter: () -> SettingsFeatureStarter
    public val editorFeatureStarter: () -> EditorFeatureStarter
    public val scheduleFeatureStarter: () -> ScheduleFeatureStarter
    public val authRepository: AuthRepository
    public val usersRepository: UsersRepository
    public val friendRequestsRepository: FriendRequestsRepository
    public val baseSchedulesRepository: BaseScheduleRepository
    public val shareSchedulesRepository: ShareSchedulesRepository
    public val organizationsRepository: OrganizationsRepository
    public val messageRepository: MessageRepository
    public val sourceSyncFacade: SourceSyncFacade
    public val deviceInfoProvider: DeviceInfoProvider
    public val startClassesReminderManager: StartClassesReminderManager
    public val endClassesReminderManager: EndClassesReminderManager
    public val homeworksReminderManager: HomeworksReminderManager
    public val workloadWarningManager: WorkloadWarningManager
    public val coroutineManager: CoroutineManager
    public val connectionManager: Konnection
    public val dateManager: DateManager
    public val crashlyticsService: CrashlyticsService
}