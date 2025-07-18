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
import io.ktor.client.plugins.cookies.CookiesStorage
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.core.database.datasource.DriverFactory
import ru.aleshin.studyassistant.core.database.datasource.ai.AiLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.listOfIntAdapter
import ru.aleshin.studyassistant.core.database.datasource.listOfStringsAdapter
import ru.aleshin.studyassistant.core.database.datasource.organizations.OrganizationsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.schedules.BaseScheduleLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.GeneralSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.NotificationSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource
import ru.aleshin.studyassistant.core.database.storages.AppwriteCookiesStorage
import ru.aleshin.studyassistant.sqldelight.ai.AiChatHistoryQueries
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageEntity
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageQueries
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
import ru.aleshin.studyassistant.sqldelight.storage.AppwriteCacheQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries

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

    bindEagerSingleton<Database> { Database(instance<SqlDriver>(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<CookiesStorage> { AppwriteCookiesStorage(instance()) }

    bindSingleton<GeneralQueries> { instance<Database>().generalQueries }
    bindSingleton<CalendarQueries> { instance<Database>().calendarQueries }
    bindSingleton<NotificationQueries> { instance<Database>().notificationQueries }
    bindSingleton<BaseScheduleQueries> { instance<Database>().baseScheduleQueries }
    bindSingleton<CustomScheduleQueries> { instance<Database>().customScheduleQueries }
    bindSingleton<HomeworkQueries> { instance<Database>().homeworkQueries }
    bindSingleton<GoalQueries> { instance<Database>().goalQueries }
    bindSingleton<TodoQueries> { instance<Database>().todoQueries }
    bindSingleton<OrganizationQueries> { instance<Database>().organizationQueries }
    bindSingleton<SubjectQueries> { instance<Database>().subjectQueries }
    bindSingleton<EmployeeQueries> { instance<Database>().employeeQueries }
    bindSingleton<AiChatHistoryQueries> { instance<Database>().aiChatHistoryQueries }
    bindSingleton<AiChatMessageQueries> { instance<Database>().aiChatMessageQueries }
    bindSingleton<AppwriteCacheQueries> { instance<Database>().appwriteCacheQueries }

    bindSingleton<AiLocalDataSource> { AiLocalDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<GeneralSettingsLocalDataSource> { GeneralSettingsLocalDataSource.Base(instance(), instance()) }
    bindSingleton<NotificationSettingsLocalDataSource> { NotificationSettingsLocalDataSource.Base(instance(), instance()) }
    bindSingleton<CalendarSettingsLocalDataSource> { CalendarSettingsLocalDataSource.Base(instance(), instance()) }
    bindSingleton<BaseScheduleLocalDataSource> { BaseScheduleLocalDataSource.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<CustomScheduleLocalDataSource> { CustomScheduleLocalDataSource.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<SubjectsLocalDataSource> { SubjectsLocalDataSource.Base(instance(), instance(), instance()) }
    bindSingleton<EmployeeLocalDataSource> { EmployeeLocalDataSource.Base(instance(), instance()) }
    bindSingleton<HomeworksLocalDataSource> { HomeworksLocalDataSource.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<DailyGoalsLocalDataSource> { DailyGoalsLocalDataSource.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<TodoLocalDataSource> { TodoLocalDataSource.Base(instance(), instance()) }
    bindSingleton<OrganizationsLocalDataSource> { OrganizationsLocalDataSource.Base(instance(), instance(), instance(), instance()) }
}