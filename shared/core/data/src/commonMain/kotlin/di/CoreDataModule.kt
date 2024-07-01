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

package di

import database.employee.EmployeeLocalDataSource
import database.organizations.OrganizationsLocalDataSource
import database.schedules.BaseScheduleLocalDataSource
import database.schedules.CustomScheduleLocalDataSource
import database.settings.CalendarSettingsLocalDataSource
import database.settings.GeneralSettingsLocalDataSource
import database.subjects.SubjectsLocalDataSource
import database.tasks.HomeworksLocalDataSource
import database.tasks.TodoLocalDataSource
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import remote.auth.AuthRemoteDataSource
import remote.employee.EmployeeRemoteDataSource
import remote.organizations.OrganizationsRemoteDataSource
import remote.schedules.BaseScheduleRemoteDataSource
import remote.schedules.CustomScheduleRemoteDataSource
import remote.settings.CalendarSettingsRemoteDataSource
import remote.subjects.SubjectsRemoteDataSource
import remote.tasks.HomeworksRemoteDataSource
import remote.tasks.TodoRemoteDataSource
import remote.users.UsersRemoteDataSource
import repositories.AuthRepository
import repositories.AuthRepositoryImpl
import repositories.BaseScheduleRepository
import repositories.BaseScheduleRepositoryImpl
import repositories.CalendarSettingsRepository
import repositories.CalendarSettingsRepositoryImpl
import repositories.CustomScheduleRepository
import repositories.CustomScheduleRepositoryImpl
import repositories.EmployeeRepository
import repositories.EmployeeRepositoryImpl
import repositories.GeneralSettingsRepository
import repositories.GeneralSettingsRepositoryImpl
import repositories.HomeworksRepository
import repositories.HomeworksRepositoryImpl
import repositories.ManageUserRepository
import repositories.ManageUserRepositoryImpl
import repositories.OrganizationsRepository
import repositories.OrganizationsRepositoryImpl
import repositories.SubjectsRepository
import repositories.SubjectsRepositoryImpl
import repositories.TodoRepository
import repositories.TodoRepositoryImpl
import repositories.UsersRepository
import repositories.UsersRepositoryImpl

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    bindSingleton<AuthRemoteDataSource> { AuthRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<AuthRepository> { AuthRepositoryImpl(instance()) }
    bindSingleton<ManageUserRepository> { ManageUserRepositoryImpl(instance()) }

    bindSingleton<UsersRemoteDataSource> { UsersRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<UsersRepository> { UsersRepositoryImpl(instance()) }

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