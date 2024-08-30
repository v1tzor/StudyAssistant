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
import ru.aleshin.studyassistant.core.data.repositories.AuthRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.BaseScheduleRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.CalendarSettingsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.CustomScheduleRepositoryImpl
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
import ru.aleshin.studyassistant.core.data.repositories.TodoRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.UsersRepositoryImpl
import ru.aleshin.studyassistant.core.database.di.coreDatabaseModule
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
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
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.remote.di.coreRemoteModule

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    importAll(coreDataPlatformModule, coreDatabaseModule, coreRemoteModule)

    bindSingleton<AuthRepository> { AuthRepositoryImpl(instance()) }
    bindSingleton<ManageUserRepository> { ManageUserRepositoryImpl(instance()) }

    bindSingleton<UsersRepository> { UsersRepositoryImpl(instance(), instance()) }

    bindSingleton<FriendRequestsRepository> { FriendRequestsRepositoryImpl(instance()) }

    bindSingleton<ShareHomeworksRepository> { ShareHomeworksRepositoryImpl(instance()) }
    bindSingleton<ShareSchedulesRepository> { ShareSchedulesRepositoryImpl(instance()) }
    bindProvider<GeneralSettingsRepository> { GeneralSettingsRepositoryImpl(instance()) }
    bindProvider<CalendarSettingsRepository> { CalendarSettingsRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<NotificationSettingsRepository> { NotificationSettingsRepositoryImpl(instance()) }
    bindProvider<BaseScheduleRepository> { BaseScheduleRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<CustomScheduleRepository> { CustomScheduleRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<SubjectsRepository> { SubjectsRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<EmployeeRepository> { EmployeeRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<HomeworksRepository> { HomeworksRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<TodoRepository> { TodoRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<OrganizationsRepository> { OrganizationsRepositoryImpl(instance(), instance(), instance()) }
    bindProvider<MessageRepository> { MessageRepositoryImpl(instance()) }
}