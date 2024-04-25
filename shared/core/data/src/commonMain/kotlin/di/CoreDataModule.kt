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
import app.cash.sqldelight.db.SqlDriver
import database.settings.GeneralSettingsLocalDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import remote.AuthRemoteDataSource
import repositories.AuthRepository
import repositories.AuthRepositoryImpl
import repositories.GeneralSettingsRepository
import repositories.GeneralSettingsRepositoryImpl
import repositories.ManageUserRepository
import repositories.ManageUserRepositoryImpl
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.sqldelight.settings.GeneralQueries

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDataModule = DI.Module("CoreData") {
    bindSingleton<FirebaseAuth> { Firebase.auth }

    bindEagerSingleton<SqlDriver> { instance<DriverFactory>().createDriver() }
    bindEagerSingleton<Database> { Database(driver = instance<SqlDriver>()) }

    bindSingleton<GeneralQueries> { instance<Database>().generalQueries }
    bindSingleton<GeneralSettingsLocalDataSource> { GeneralSettingsLocalDataSource.Base(instance(), instance()) }
    bindProvider<GeneralSettingsRepository> { GeneralSettingsRepositoryImpl(instance()) }

    bindSingleton<AuthRemoteDataSource> { AuthRemoteDataSource.Base(instance()) }
    bindSingleton<AuthRepository> { AuthRepositoryImpl(instance()) }
    bindSingleton<ManageUserRepository> { ManageUserRepositoryImpl(instance()) }
}