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
import ru.aleshin.studyassistant.core.data.repositories.OrganizationsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.SubjectsRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.TodoRepositoryImpl
import ru.aleshin.studyassistant.core.data.repositories.UsersRepositoryImpl
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.organizations.OrganizationsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.schedules.BaseScheduleLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.settings.GeneralSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.tasks.TodoLocalDataSource
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.remote.datasources.auth.AuthRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.employee.EmployeeRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.organizations.OrganizationsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.requests.FriendRequestsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.schedules.BaseScheduleRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.schedules.CustomScheduleRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.settings.CalendarSettingsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.subjects.SubjectsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.tasks.HomeworksRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.tasks.TodoRemoteDataSource
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    bindSingleton<AuthRemoteDataSource> { AuthRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<AuthRepository> { AuthRepositoryImpl(instance()) }
    bindSingleton<ManageUserRepository> { ManageUserRepositoryImpl(instance()) }

    bindSingleton<UsersRemoteDataSource> { UsersRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<UsersRepository> { UsersRepositoryImpl(instance()) }

    bindSingleton<FriendRequestsRemoteDataSource> { FriendRequestsRemoteDataSource.Base(instance()) }
    bindSingleton<FriendRequestsRepository> { FriendRequestsRepositoryImpl(instance()) }

    bindSingleton<GeneralSettingsLocalDataSource> {
        GeneralSettingsLocalDataSource.Base(instance(), instance())
    }
    bindProvider<GeneralSettingsRepository> {
        GeneralSettingsRepositoryImpl(instance())
    }

    bindSingleton<CalendarSettingsRemoteDataSource> {
        CalendarSettingsRemoteDataSource.Base(instance())
    }
    bindSingleton<CalendarSettingsLocalDataSource> {
        CalendarSettingsLocalDataSource.Base(instance(), instance())
    }
    bindProvider<CalendarSettingsRepository> {
        CalendarSettingsRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<BaseScheduleLocalDataSource> {
        BaseScheduleLocalDataSource.Base(instance(), instance(), instance(), instance(), instance())
    }
    bindSingleton<BaseScheduleRemoteDataSource> {
        BaseScheduleRemoteDataSource.Base(instance())
    }
    bindProvider<BaseScheduleRepository> {
        BaseScheduleRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<CustomScheduleLocalDataSource> {
        CustomScheduleLocalDataSource.Base(instance(), instance(), instance(), instance(), instance())
    }
    bindSingleton<CustomScheduleRemoteDataSource> {
        CustomScheduleRemoteDataSource.Base(instance())
    }
    bindProvider<CustomScheduleRepository> {
        CustomScheduleRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<SubjectsLocalDataSource> {
        SubjectsLocalDataSource.Base(instance(), instance(), instance())
    }
    bindSingleton<SubjectsRemoteDataSource> {
        SubjectsRemoteDataSource.Base(instance())
    }
    bindProvider<SubjectsRepository> {
        SubjectsRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<EmployeeLocalDataSource> {
        EmployeeLocalDataSource.Base(instance(), instance())
    }
    bindSingleton<EmployeeRemoteDataSource> {
        EmployeeRemoteDataSource.Base(instance())
    }
    bindProvider<EmployeeRepository> {
        EmployeeRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<HomeworksLocalDataSource> {
        HomeworksLocalDataSource.Base(instance(), instance(), instance(), instance(), instance())
    }
    bindSingleton<HomeworksRemoteDataSource> {
        HomeworksRemoteDataSource.Base(instance())
    }
    bindProvider<HomeworksRepository> {
        HomeworksRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<TodoLocalDataSource> {
        TodoLocalDataSource.Base(instance(), instance())
    }
    bindSingleton<TodoRemoteDataSource> {
        TodoRemoteDataSource.Base(instance())
    }
    bindProvider<TodoRepository> {
        TodoRepositoryImpl(instance(), instance(), instance())
    }

    bindSingleton<OrganizationsLocalDataSource> {
        OrganizationsLocalDataSource.Base(instance(), instance(), instance(), instance())
    }
    bindSingleton<OrganizationsRemoteDataSource> {
        OrganizationsRemoteDataSource.Base(instance())
    }
    bindProvider<OrganizationsRepository> {
        OrganizationsRepositoryImpl(instance(), instance(), instance())
    }
}