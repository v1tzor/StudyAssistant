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

package ru.aleshin.studyassistant.core.domain.managers.sync

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import ru.aleshin.studyassistant.core.common.managers.CoroutineFlow.BACKGROUND
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 25.07.2025.
 */
interface SourceSyncFacade {

    suspend fun syncAllSource()

    suspend fun singleSyncAllSources(): Boolean

    suspend fun stopAllSourceSync()

    suspend fun clearAllSyncedData()

    class Base(
        currentUserSourceSyncManager: CurrentUserSourceSyncManager,
        baseScheduleSourceSyncManager: BaseScheduleSourceSyncManager,
        customScheduleSourceSyncManager: CustomScheduleSourceSyncManager,
        todoSourceSyncManager: TodoSourceSyncManager,
        homeworkSourceSyncManager: HomeworkSourceSyncManager,
        goalsSourceSyncManager: DailyGoalsSourceSyncManager,
        employeeSourceSyncManager: EmployeeSourceSyncManager,
        organizationsSourceSyncManager: OrganizationsSourceSyncManager,
        subjectsSourceSyncManager: SubjectsSourceSyncManager,
        sharedSchedulesSourceSyncManager: SharedSchedulesSourceSyncManager,
        sharedHomeworksSourceSyncManager: SharedHomeworksSourceSyncManager,
        friendRequestsSourceSyncManager: FriendRequestsSourceSyncManager,
        dailyAiStatisticsSourceSyncManager: DailyAiStatisticsSourceSyncManager,
        private val coroutineManager: CoroutineManager,
    ) : SourceSyncFacade {

        private val allSyncManagers = listOf(
            currentUserSourceSyncManager,
            baseScheduleSourceSyncManager,
            customScheduleSourceSyncManager,
            todoSourceSyncManager,
            homeworkSourceSyncManager,
            goalsSourceSyncManager,
            employeeSourceSyncManager,
            organizationsSourceSyncManager,
            subjectsSourceSyncManager,
            sharedSchedulesSourceSyncManager,
            sharedHomeworksSourceSyncManager,
            friendRequestsSourceSyncManager,
            dailyAiStatisticsSourceSyncManager,
        )

        override suspend fun syncAllSource() {
            coroutineManager.changeFlow(BACKGROUND) {
                allSyncManagers.map { async { it.startSourceSync() } }.awaitAll()
            }
        }

        override suspend fun singleSyncAllSources(): Boolean {
            return coroutineManager.changeFlow(BACKGROUND) {
                allSyncManagers.map { async { it.singleSyncRound() } }.awaitAll().all { it }
            }
        }

        override suspend fun stopAllSourceSync() {
            coroutineManager.changeFlow(BACKGROUND) {
                allSyncManagers.forEach { it.stopSourceSync() }
            }
        }

        override suspend fun clearAllSyncedData() {
            coroutineManager.changeFlow(BACKGROUND) {
                allSyncManagers.forEach { it.clearSourceData() }
            }
        }
    }
}