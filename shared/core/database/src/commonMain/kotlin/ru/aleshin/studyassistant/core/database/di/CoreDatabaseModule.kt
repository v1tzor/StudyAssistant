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

package ru.aleshin.studyassistant.core.database.di

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.core.database.datasource.DriverFactory
import ru.aleshin.studyassistant.core.database.datasource.ai.AiLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.ai.DailyAiStatisticsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.listOfIntAdapter
import ru.aleshin.studyassistant.core.database.datasource.listOfStringsAdapter
import ru.aleshin.studyassistant.core.database.datasource.organizations.OrganizationsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.requests.FriendRequestsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.schedules.BaseScheduleLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.GeneralSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.NotificationSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.shared.SharedHomeworksLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.shared.SharedSchedulesLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.user.UserLocalDataSource
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.sqldelight.ai.AiChatHistoryQueries
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageEntity
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageQueries
import ru.aleshin.studyassistant.sqldelight.ai.DailyAiResponsesQueries
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.goals.GoalQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleEntity
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleQueries
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleEntity
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleQueries
import ru.aleshin.studyassistant.sqldelight.settings.CalendarQueries
import ru.aleshin.studyassistant.sqldelight.settings.CalendarSettingsEntity
import ru.aleshin.studyassistant.sqldelight.settings.GeneralQueries
import ru.aleshin.studyassistant.sqldelight.settings.NotificationQueries
import ru.aleshin.studyassistant.sqldelight.settings.NotificationSettingsEntity
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedHomeworksEntity
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedHomeworksQueries
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedSchedulesEntity
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedSchedulesQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.sync.OfflineChangeQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries
import ru.aleshin.studyassistant.sqldelight.user.CurrentFriendRequestsEntity
import ru.aleshin.studyassistant.sqldelight.user.CurrentFriendRequestsQueries
import ru.aleshin.studyassistant.sqldelight.user.CurrentUserEntity
import ru.aleshin.studyassistant.sqldelight.user.CurrentUserQueries

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDatabaseModule = DI.Module("CoreDatabase") {
    import(coreDatabasePlatformModule)

    bindEagerSingleton<SqlDriver> { instance<DriverFactory>().createDriver() }
    bindEagerSingleton<ColumnAdapter<List<String>, String>> { listOfStringsAdapter }
    bindEagerSingleton<ColumnAdapter<List<Int>, String>> { listOfIntAdapter }
    bindEagerSingleton<CalendarSettingsEntity.Adapter> { CalendarSettingsEntity.Adapter(instance()) }
    bindEagerSingleton<NotificationSettingsEntity.Adapter> { NotificationSettingsEntity.Adapter(instance(), instance()) }
    bindEagerSingleton<CustomScheduleEntity.Adapter> { CustomScheduleEntity.Adapter(instance()) }
    bindEagerSingleton<BaseScheduleEntity.Adapter> { BaseScheduleEntity.Adapter(instance()) }
    bindEagerSingleton<OrganizationEntity.Adapter> { OrganizationEntity.Adapter(instance(), instance(), instance(), instance(), instance()) }
    bindEagerSingleton<EmployeeEntity.Adapter> { EmployeeEntity.Adapter(instance(), instance(), instance(), instance()) }
    bindEagerSingleton<AiChatMessageEntity.Adapter> { AiChatMessageEntity.Adapter(instance()) }
    bindEagerSingleton<CurrentUserEntity.Adapter> { CurrentUserEntity.Adapter(instance(), instance(), instance()) }
    bindEagerSingleton<CurrentSharedSchedulesEntity.Adapter> { CurrentSharedSchedulesEntity.Adapter(instance(), instance()) }
    bindEagerSingleton<CurrentSharedHomeworksEntity.Adapter> { CurrentSharedHomeworksEntity.Adapter(instance(), instance()) }
    bindEagerSingleton<CurrentFriendRequestsEntity.Adapter> { CurrentFriendRequestsEntity.Adapter(instance(), instance(), instance()) }

    bindEagerSingleton<Database> { Database(instance<SqlDriver>(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<OfflineChangeQueries> { instance<Database>().offlineChangeQueries }
    bindSingleton<ChangeQueueStorage> { ChangeQueueStorage.Base(instance()) }

    bindSingleton<AiChatHistoryQueries> { instance<Database>().aiChatHistoryQueries }
    bindSingleton<AiChatMessageQueries> { instance<Database>().aiChatMessageQueries }
    bindSingleton<DailyAiResponsesQueries> { instance<Database>().dailyAiResponsesQueries }
    bindSingleton<AiLocalDataSource> { AiLocalDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<DailyAiStatisticsLocalDataSource> { DailyAiStatisticsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<GeneralQueries> { instance<Database>().generalQueries }
    bindSingleton<GeneralSettingsLocalDataSource> { GeneralSettingsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<CurrentSharedSchedulesQueries> { instance<Database>().currentSharedSchedulesQueries }
    bindSingleton<SharedSchedulesLocalDataSource> { SharedSchedulesLocalDataSource.Base(instance(), instance()) }

    bindSingleton<CurrentFriendRequestsQueries> { instance<Database>().currentFriendRequestsQueries }
    bindSingleton<FriendRequestsLocalDataSource> { FriendRequestsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<CurrentSharedHomeworksQueries> { instance<Database>().currentSharedHomeworksQueries }
    bindSingleton<SharedHomeworksLocalDataSource> { SharedHomeworksLocalDataSource.Base(instance(), instance()) }

    bindSingleton<CurrentUserQueries> { instance<Database>().currentUserQueries }
    bindSingleton<UserLocalDataSource> { UserLocalDataSource.Base(instance(), instance()) }

    bindSingleton<NotificationQueries> { instance<Database>().notificationQueries }
    bindSingleton<NotificationSettingsLocalDataSource> { NotificationSettingsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<CalendarQueries> { instance<Database>().calendarQueries }
    bindSingleton<CalendarSettingsLocalDataSource.OfflineStorage> { CalendarSettingsLocalDataSource.OfflineStorage.Base(instance(), instance()) }
    bindSingleton<CalendarSettingsLocalDataSource.SyncStorage> { CalendarSettingsLocalDataSource.SyncStorage.Base(instance(), instance()) }
    bindSingleton<CalendarSettingsLocalDataSource> { CalendarSettingsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<BaseScheduleQueries> { instance<Database>().baseScheduleQueries }
    bindSingleton<BaseScheduleLocalDataSource.SyncStorage> { BaseScheduleLocalDataSource.SyncStorage.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<BaseScheduleLocalDataSource.OfflineStorage> { BaseScheduleLocalDataSource.OfflineStorage.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<BaseScheduleLocalDataSource> { BaseScheduleLocalDataSource.Base(instance(), instance()) }

    bindSingleton<CustomScheduleQueries> { instance<Database>().customScheduleQueries }
    bindSingleton<CustomScheduleLocalDataSource.SyncStorage> { CustomScheduleLocalDataSource.SyncStorage.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<CustomScheduleLocalDataSource.OfflineStorage> { CustomScheduleLocalDataSource.OfflineStorage.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<CustomScheduleLocalDataSource> { CustomScheduleLocalDataSource.Base(instance(), instance()) }

    bindSingleton<EmployeeQueries> { instance<Database>().employeeQueries }
    bindSingleton<EmployeeLocalDataSource.SyncStorage> { EmployeeLocalDataSource.SyncStorage.Base(instance(), instance()) }
    bindSingleton<EmployeeLocalDataSource.OfflineStorage> { EmployeeLocalDataSource.OfflineStorage.Base(instance(), instance()) }
    bindSingleton<EmployeeLocalDataSource> { EmployeeLocalDataSource.Base(instance(), instance()) }

    bindSingleton<GoalQueries> { instance<Database>().goalQueries }
    bindSingleton<DailyGoalsLocalDataSource.SyncStorage> { DailyGoalsLocalDataSource.SyncStorage.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<DailyGoalsLocalDataSource.OfflineStorage> { DailyGoalsLocalDataSource.OfflineStorage.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<DailyGoalsLocalDataSource> { DailyGoalsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<OrganizationQueries> { instance<Database>().organizationQueries }
    bindSingleton<OrganizationsLocalDataSource.SyncStorage> { OrganizationsLocalDataSource.SyncStorage.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<OrganizationsLocalDataSource.OfflineStorage> { OrganizationsLocalDataSource.OfflineStorage.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<OrganizationsLocalDataSource> { OrganizationsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<SubjectQueries> { instance<Database>().subjectQueries }
    bindSingleton<SubjectsLocalDataSource.OfflineStorage> { SubjectsLocalDataSource.OfflineStorage.Base(instance(), instance(), instance()) }
    bindSingleton<SubjectsLocalDataSource.SyncStorage> { SubjectsLocalDataSource.SyncStorage.Base(instance(), instance(), instance()) }
    bindSingleton<SubjectsLocalDataSource> { SubjectsLocalDataSource.Base(instance(), instance()) }

    bindSingleton<HomeworkQueries> { instance<Database>().homeworkQueries }
    bindSingleton<HomeworksLocalDataSource.OfflineStorage> { HomeworksLocalDataSource.OfflineStorage.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworksLocalDataSource.SyncStorage> { HomeworksLocalDataSource.SyncStorage.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworksLocalDataSource> { HomeworksLocalDataSource.Base(instance(), instance()) }

    bindSingleton<TodoQueries> { instance<Database>().todoQueries }
    bindSingleton<TodoLocalDataSource.OfflineStorage> { TodoLocalDataSource.OfflineStorage.Base(instance(), instance()) }
    bindSingleton<TodoLocalDataSource.SyncStorage> { TodoLocalDataSource.SyncStorage.Base(instance(), instance()) }
    bindSingleton<TodoLocalDataSource> { TodoLocalDataSource.Base(instance(), instance()) }
}