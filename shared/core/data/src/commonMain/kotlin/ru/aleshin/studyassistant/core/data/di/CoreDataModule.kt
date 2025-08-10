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

package ru.aleshin.studyassistant.core.data.di

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.api.di.coreClintApiModule
import ru.aleshin.studyassistant.core.data.managers.reminders.TodoReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.BaseScheduleSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.CalendarSettingsSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.CurrentUserSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.CustomScheduleSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.DailyAiStatisticsSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.DailyGoalsSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.EmployeeSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.FriendRequestsSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.HomeworkSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.OrganizationsSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.SharedHomeworksSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.SharedSchedulesSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.SubjectsSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.TodoSourceSyncManagerImpl
import ru.aleshin.studyassistant.core.data.mappers.ai.DailyAiResponsesSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.goals.GoalSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.organizations.OrganizationSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.requsts.FriendRequestsSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.schedules.BaseScheduleSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.schedules.CustomScheduleSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.settings.CalendarSettingsSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.share.ShareHomeworksSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.share.SharedSchedulesSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.subjects.SubjectSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.tasks.HomeworkSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.tasks.TodoSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.users.AppUserSyncMapper
import ru.aleshin.studyassistant.core.data.mappers.users.EmployeeSyncMapper
import ru.aleshin.studyassistant.core.data.repositories.AiAssistantRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.AuthRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.BaseScheduleRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.CalendarSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.CustomScheduleRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.DailyAiStatisticsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.DailyGoalsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.EmployeeRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.FriendRequestsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.GeneralSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.HomeworksRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.ManageUserRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.MessageRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.NotificationSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.OrganizationsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.ShareHomeworksRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.ShareSchedulesRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.SubjectsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.SubscriptionsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.TodoRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.UsersRepositoryImpl
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.di.coreDatabaseModule
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.managers.sync.BaseScheduleSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.CalendarSettingsSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.CurrentUserSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.CustomScheduleSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.DailyAiStatisticsSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.DailyGoalsSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.EmployeeSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.FriendRequestsSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.HomeworkSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.OrganizationsSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.SharedHomeworksSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.SharedSchedulesSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.SubjectsSourceSyncManager
import ru.aleshin.studyassistant.core.domain.managers.sync.TodoSourceSyncManager
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyAiStatisticsRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubscriptionsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.remote.di.coreRemoteModule

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    importAll(coreDataPlatformModule, coreDatabaseModule, coreRemoteModule, coreClintApiModule)

    bindSingleton<RemoteResultSyncHandler> { RemoteResultSyncHandler.Base(instance(), instance()) }
    bindSingleton<SubscriptionChecker> { SubscriptionChecker.Base(instance(), instance()) }

    bindSingleton<TodoSyncMapper> { TodoSyncMapper() }
    bindSingleton<TodoSourceSyncManager> { TodoSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<TodoRepository> { TodoRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<EmployeeSyncMapper> { EmployeeSyncMapper() }
    bindSingleton<EmployeeSourceSyncManager> { EmployeeSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<EmployeeRepository> { EmployeeRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<SubjectSyncMapper> { SubjectSyncMapper() }
    bindSingleton<SubjectsSourceSyncManager> { SubjectsSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<SubjectsRepository> { SubjectsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<OrganizationSyncMapper> { OrganizationSyncMapper() }
    bindSingleton<OrganizationsSourceSyncManager> { OrganizationsSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<OrganizationsRepository> { OrganizationsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<BaseScheduleSyncMapper> { BaseScheduleSyncMapper() }
    bindSingleton<BaseScheduleSourceSyncManager> { BaseScheduleSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<BaseScheduleRepository> { BaseScheduleRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<CustomScheduleSyncMapper> { CustomScheduleSyncMapper() }
    bindSingleton<CustomScheduleSourceSyncManager> { CustomScheduleSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<CustomScheduleRepository> { CustomScheduleRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<HomeworkSyncMapper> { HomeworkSyncMapper() }
    bindSingleton<HomeworkSourceSyncManager> { HomeworkSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<HomeworksRepository> { HomeworksRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<GoalSyncMapper> { GoalSyncMapper() }
    bindSingleton<DailyGoalsSourceSyncManager> { DailyGoalsSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<DailyGoalsRepository> { DailyGoalsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<CalendarSettingsSyncMapper> { CalendarSettingsSyncMapper() }
    bindSingleton<CalendarSettingsSourceSyncManager> { CalendarSettingsSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<CalendarSettingsRepository> { CalendarSettingsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<FriendRequestsSyncMapper> { FriendRequestsSyncMapper() }
    bindSingleton<FriendRequestsSourceSyncManager> { FriendRequestsSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<FriendRequestsRepository> { FriendRequestsRepositoryImpl(instance(), instance(), instance(), instance()) }

    bindSingleton<SharedSchedulesSyncMapper> { SharedSchedulesSyncMapper() }
    bindSingleton<SharedSchedulesSourceSyncManager> { SharedSchedulesSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ShareSchedulesRepository> { ShareSchedulesRepositoryImpl(instance(), instance(), instance(), instance(),) }

    bindSingleton<ShareHomeworksSyncMapper> { ShareHomeworksSyncMapper() }
    bindSingleton<SharedHomeworksSourceSyncManager> { SharedHomeworksSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ShareHomeworksRepository> { ShareHomeworksRepositoryImpl(instance(), instance(), instance(), instance()) }

    bindSingleton<AppUserSyncMapper> { AppUserSyncMapper() }
    bindSingleton<CurrentUserSourceSyncManager> { CurrentUserSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(),) }
    bindProvider<UsersRepository> { UsersRepositoryImpl(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<DailyAiResponsesSyncMapper> { DailyAiResponsesSyncMapper() }
    bindSingleton<DailyAiStatisticsSourceSyncManager> { DailyAiStatisticsSourceSyncManagerImpl(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<DailyAiStatisticsRepository> { DailyAiStatisticsRepositoryImpl(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<AiAssistantRepository> { AiAssistantRepositoryImpl(instance(), instance(), instance()) }

    bindSingleton<GeneralSettingsRepository> { GeneralSettingsRepositoryImpl(instance()) }

    bindSingleton<NotificationSettingsRepository> { NotificationSettingsRepositoryImpl(instance()) }

    bindSingleton<AuthRepository> { AuthRepositoryImpl(instance()) }
    bindSingleton<ManageUserRepository> { ManageUserRepositoryImpl(instance()) }
    bindSingleton<SubscriptionsRepository> { SubscriptionsRepositoryImpl(instance()) }
    bindSingleton<MessageRepository> { MessageRepositoryImpl(instance()) }

    bindProvider<TodoReminderManager> { TodoReminderManagerImpl(instance(), instance(), instance()) }
}