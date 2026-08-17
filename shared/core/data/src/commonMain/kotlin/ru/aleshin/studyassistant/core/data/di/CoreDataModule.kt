/*
 * Copyright 2026 Stanislav Aleshin
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
import ru.aleshin.studyassistant.core.data.handlers.AiCompletionHandler
import ru.aleshin.studyassistant.core.data.handlers.AiConversationHandler
import ru.aleshin.studyassistant.core.data.handlers.AiSettingsHandler
import ru.aleshin.studyassistant.core.data.managers.InstallationIdProviderImpl
import ru.aleshin.studyassistant.core.data.managers.reminders.TodoReminderManagerImpl
import ru.aleshin.studyassistant.core.data.repositories.AdRewardRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.AiAssistantRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.AiSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.AnalyticsSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.BaseScheduleRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.CalendarSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.CustomScheduleRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.DailyGoalsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.EmployeeRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.GeneralSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.HomeworkShareRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.HomeworksRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.NotificationSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.OrganizationsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.ProfileRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.ScheduleImportRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.ScheduleShareRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.SubjectsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.TodoRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.UserDataResetRepositoryImpl
import ru.aleshin.studyassistant.core.database.di.coreDatabaseModule
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.AnalyticsSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworkShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UserDataResetRepository
import ru.aleshin.studyassistant.core.remote.di.coreRemoteModule

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    importAll(coreDataPlatformModule, coreDatabaseModule, coreRemoteModule)

    bindProvider<TodoRepository> { TodoRepositoryImpl(instance()) }
    bindProvider<EmployeeRepository> { EmployeeRepositoryImpl(instance(), instance()) }
    bindProvider<SubjectsRepository> { SubjectsRepositoryImpl(instance()) }
    bindProvider<OrganizationsRepository> { OrganizationsRepositoryImpl(instance(), instance()) }
    bindProvider<BaseScheduleRepository> { BaseScheduleRepositoryImpl(instance()) }
    bindProvider<CustomScheduleRepository> { CustomScheduleRepositoryImpl(instance()) }
    bindProvider<HomeworksRepository> { HomeworksRepositoryImpl(instance()) }
    bindProvider<DailyGoalsRepository> { DailyGoalsRepositoryImpl(instance()) }
    bindProvider<CalendarSettingsRepository> { CalendarSettingsRepositoryImpl(instance()) }
    bindSingleton<ProfileRepository> { ProfileRepositoryImpl(instance(), instance()) }

    bindSingleton<InstallationIdProvider> {
        InstallationIdProviderImpl(
            secureDataSource = instance(),
            remoteDataSource = instance(),
        )
    }
    bindProvider<ScheduleShareRepository> { ScheduleShareRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<HomeworkShareRepository> { HomeworkShareRepositoryImpl(instance(), instance(), instance(), instance()) }
    bindProvider<ScheduleImportRepository> { ScheduleImportRepositoryImpl(instance(), instance(), instance()) }

    bindSingleton<AiSettingsHandler> { AiSettingsHandler.Base(instance(), instance()) }
    bindSingleton<AiCompletionHandler> { AiCompletionHandler.Base(instance(), instance(), instance()) }
    bindSingleton<AiConversationHandler> { AiConversationHandler.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<AiSettingsRepository> { AiSettingsRepositoryImpl(instance()) }
    bindSingleton<AdRewardRepository> { AdRewardRepositoryImpl(instance(), instance(), instance()) }
    bindSingleton<AiAssistantRepository> { AiAssistantRepositoryImpl(instance(), instance()) }

    bindSingleton<GeneralSettingsRepository> { GeneralSettingsRepositoryImpl(instance()) }
    bindSingleton<NotificationSettingsRepository> { NotificationSettingsRepositoryImpl(instance()) }
    bindSingleton<AnalyticsSettingsRepository> { AnalyticsSettingsRepositoryImpl(instance()) }
    bindSingleton<UserDataResetRepository> { UserDataResetRepositoryImpl(instance()) }

    bindProvider<TodoReminderManager> { TodoReminderManagerImpl(instance(), instance()) }
}
