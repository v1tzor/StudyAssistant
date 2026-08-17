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

package ru.aleshin.studyassistant.core.database.datasource.settings

import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import kotlin.time.Clock

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
interface UserDataResetLocalDataSource {

    suspend fun deleteAllSchedules()
    suspend fun deleteSchedulesByOrganizations(organizationIds: Set<UID>)
    suspend fun deleteAllUserData()

    class Base(
        private val database: Database,
    ) : UserDataResetLocalDataSource {

        override suspend fun deleteAllSchedules() {
            database.transaction {
                database.baseScheduleQueries.deleteAllSchedules()
                database.customScheduleQueries.deleteAllSchedules()
            }
        }

        override suspend fun deleteSchedulesByOrganizations(organizationIds: Set<UID>) {
            if (organizationIds.isEmpty()) return
            val updatedAt = Clock.System.now().toEpochMilliseconds()
            database.transaction {
                val removableBaseIds = mutableListOf<String>()
                database.baseScheduleQueries.fetchAllSchedules().executeAsList().forEach { schedule ->
                    val remainingClasses = schedule.classes.filterNot { classJson ->
                        Json.decodeFromString<ClassEntity>(classJson).organizationId in organizationIds
                    }
                    when {
                        remainingClasses.isEmpty() -> removableBaseIds.add(schedule.uid)
                        remainingClasses.size != schedule.classes.size -> {
                            database.baseScheduleQueries.addOrUpdateSchedule(
                                schedule.copy(classes = remainingClasses, updated_at = updatedAt),
                            )
                        }
                    }
                }
                if (removableBaseIds.isNotEmpty()) {
                    database.baseScheduleQueries.deleteSchedulesById(removableBaseIds)
                }

                val removableCustomIds = mutableListOf<String>()
                database.customScheduleQueries.fetchAllSchedules().executeAsList().forEach { schedule ->
                    val remainingClasses = schedule.classes.filterNot { classJson ->
                        Json.decodeFromString<ClassEntity>(classJson).organizationId in organizationIds
                    }
                    when {
                        remainingClasses.isEmpty() -> removableCustomIds.add(schedule.uid)
                        remainingClasses.size != schedule.classes.size -> {
                            database.customScheduleQueries.addOrUpdateSchedule(
                                schedule.copy(classes = remainingClasses, updated_at = updatedAt),
                            )
                        }
                    }
                }
                if (removableCustomIds.isNotEmpty()) {
                    database.customScheduleQueries.deleteSchedulesById(removableCustomIds)
                }
            }
        }

        override suspend fun deleteAllUserData() {
            database.transaction {
                database.baseScheduleQueries.deleteAllSchedules()
                database.customScheduleQueries.deleteAllSchedules()
                database.homeworkQueries.deleteAllHomeworks()
                database.todoQueries.deleteAllTodos()
                database.goalQueries.deleteAllGoals()
                database.subjectQueries.deleteAllSubjects()
                database.employeeQueries.deleteAllEmployees()
                database.organizationQueries.deleteAllOrganizations()
                database.profileQueries.deleteProfile()
                database.homeworkShareReceiptQueries.deleteAllReceipts()
                database.aiChatMessageQueries.deleteAllMessages()
                database.aiChatHistoryQueries.deleteAllChats()
                database.analyticsQueries.resetSettings()
            }
        }
    }
}
