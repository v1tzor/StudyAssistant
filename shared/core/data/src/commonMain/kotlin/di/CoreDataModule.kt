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

import DriverFactory
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import database.classes.ClassLocalDataSource
import database.listOfStringsAdapter
import database.organizations.OrganizationsLocalDataSource
import database.settings.CalendarSettingsLocalDataSource
import database.settings.GeneralSettingsLocalDataSource
import database.tasks.HomeworksLocalDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import remote.auth.AuthRemoteDataSource
import remote.classes.ClassRemoteDataSource
import remote.organizations.OrganizationsRemoteDataSource
import remote.settings.CalendarSettingsRemoteDataSource
import remote.tasks.HomeworksRemoteDataSource
import remote.users.UsersRemoteDataSource
import repositories.AuthRepository
import repositories.AuthRepositoryImpl
import repositories.CalendarSettingsRepository
import repositories.CalendarSettingsRepositoryImpl
import repositories.ClassRepository
import repositories.ClassRepositoryImpl
import repositories.GeneralSettingsRepository
import repositories.GeneralSettingsRepositoryImpl
import repositories.HomeworksRepository
import repositories.HomeworksRepositoryImpl
import repositories.ManageUserRepository
import repositories.ManageUserRepositoryImpl
import repositories.OrganizationsRepository
import repositories.OrganizationsRepositoryImpl
import repositories.UsersRepository
import repositories.UsersRepositoryImpl
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.sqldelight.`class`.ClassQueries
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.settings.CalendarQueries
import ru.aleshin.studyassistant.sqldelight.settings.GeneralQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    bindSingleton<FirebaseAuth> { Firebase.auth }
    bindSingleton<FirebaseFirestore> { Firebase.firestore }

    bindEagerSingleton<SqlDriver> { instance<DriverFactory>().createDriver() }
    bindEagerSingleton<ColumnAdapter<List<String>, String>> { listOfStringsAdapter }
    bindEagerSingleton<OrganizationEntity.Adapter> { OrganizationEntity.Adapter(instance(), instance(), instance(), instance()) }
    bindEagerSingleton<EmployeeEntity.Adapter> { EmployeeEntity.Adapter(instance(), instance(), instance(), instance()) }
    bindEagerSingleton<Database> { Database(instance<SqlDriver>(), instance(), instance()) }

    bindSingleton<GeneralQueries> { instance<Database>().generalQueries }
    bindSingleton<GeneralSettingsLocalDataSource> { GeneralSettingsLocalDataSource.Base(instance(), instance()) }
    bindProvider<GeneralSettingsRepository> { GeneralSettingsRepositoryImpl(instance()) }

    bindSingleton<CalendarQueries> { instance<Database>().calendarQueries }
    bindSingleton<CalendarSettingsRemoteDataSource> { CalendarSettingsRemoteDataSource.Base(instance()) }
    bindSingleton<CalendarSettingsLocalDataSource> { CalendarSettingsLocalDataSource.Base(instance(), instance()) }
    bindProvider<CalendarSettingsRepository> { CalendarSettingsRepositoryImpl(instance(), instance(), instance()) }

    bindSingleton<AuthRemoteDataSource> { AuthRemoteDataSource.Base(instance()) }
    bindSingleton<AuthRepository> { AuthRepositoryImpl(instance()) }
    bindSingleton<ManageUserRepository> { ManageUserRepositoryImpl(instance()) }

    bindSingleton<UsersRemoteDataSource> { UsersRemoteDataSource.Base(instance(), instance()) }
    bindSingleton<UsersRepository> { UsersRepositoryImpl(instance()) }

    bindSingleton<OrganizationQueries> { instance<Database>().organizationQueries }
    bindSingleton<OrganizationsLocalDataSource> { OrganizationsLocalDataSource.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<OrganizationsRemoteDataSource> { OrganizationsRemoteDataSource.Base(instance()) }
    bindProvider<OrganizationsRepository> { OrganizationsRepositoryImpl(instance(), instance(), instance()) }

    bindSingleton<SubjectQueries> { instance<Database>().subjectQueries }

    bindSingleton<EmployeeQueries> { instance<Database>().employeeQueries }

    bindSingleton<HomeworkQueries> { instance<Database>().homeworkQueries }
    bindSingleton<HomeworksLocalDataSource> { HomeworksLocalDataSource.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworksRemoteDataSource> { HomeworksRemoteDataSource.Base(instance()) }
    bindProvider<HomeworksRepository> { HomeworksRepositoryImpl(instance(), instance(), instance()) }

    bindSingleton<ClassQueries> { instance<Database>().classQueries }
    bindSingleton<ClassLocalDataSource> { ClassLocalDataSource.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<ClassRemoteDataSource> { ClassRemoteDataSource.Base(instance()) }
    bindProvider<ClassRepository> { ClassRepositoryImpl(instance(), instance(), instance()) }
}
