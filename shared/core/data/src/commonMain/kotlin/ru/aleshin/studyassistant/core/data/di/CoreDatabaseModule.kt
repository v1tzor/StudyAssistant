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

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.core.database.datasource.DriverFactory
import ru.aleshin.studyassistant.core.database.datasource.listOfIntAdapter
import ru.aleshin.studyassistant.core.database.datasource.listOfStringsAdapter
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleEntity
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleQueries
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleEntity
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleQueries
import ru.aleshin.studyassistant.sqldelight.settings.CalendarQueries
import ru.aleshin.studyassistant.sqldelight.settings.GeneralQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
val coreDatabaseModule = DI.Module("CoreDatabase") {
    bindSingleton<FirebaseAuth> { Firebase.auth }
    bindSingleton<FirebaseFirestore> { Firebase.firestore }

    bindEagerSingleton<SqlDriver> { instance<DriverFactory>().createDriver() }
    bindEagerSingleton<ColumnAdapter<List<String>, String>> { listOfStringsAdapter }
    bindEagerSingleton<ColumnAdapter<List<Int>, String>> { listOfIntAdapter }
    bindEagerSingleton<CustomScheduleEntity.Adapter> { CustomScheduleEntity.Adapter(instance()) }
    bindEagerSingleton<BaseScheduleEntity.Adapter> { BaseScheduleEntity.Adapter(instance()) }
    bindEagerSingleton<OrganizationEntity.Adapter> {
        OrganizationEntity.Adapter(instance(), instance(), instance(), instance(), instance())
    }
    bindEagerSingleton<EmployeeEntity.Adapter> {
        EmployeeEntity.Adapter(instance(), instance(), instance(), instance())
    }

    bindEagerSingleton<Database> { Database(instance<SqlDriver>(), instance(), instance(), instance(), instance()) }

    bindSingleton<GeneralQueries> { instance<Database>().generalQueries }
    bindSingleton<CalendarQueries> { instance<Database>().calendarQueries }
    bindSingleton<BaseScheduleQueries> { instance<Database>().baseScheduleQueries }
    bindSingleton<CustomScheduleQueries> { instance<Database>().customScheduleQueries }
    bindSingleton<HomeworkQueries> { instance<Database>().homeworkQueries }
    bindSingleton<TodoQueries> { instance<Database>().todoQueries }
    bindSingleton<OrganizationQueries> { instance<Database>().organizationQueries }
    bindSingleton<SubjectQueries> { instance<Database>().subjectQueries }
    bindSingleton<EmployeeQueries> { instance<Database>().employeeQueries }
}