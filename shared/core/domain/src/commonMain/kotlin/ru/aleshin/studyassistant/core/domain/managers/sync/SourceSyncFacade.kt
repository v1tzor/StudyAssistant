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

/**
 * @author Stanislav Aleshin on 25.07.2025.
 */
interface SourceSyncFacade {

    suspend fun startAllSourceSync()

    fun stopAllSourceSync()

    suspend fun clearAllSyncedData()

    class Base(
        private val currentUserSourceSyncManager: CurrentUserSourceSyncManager,
        private val baseScheduleSourceSyncManager: BaseScheduleSourceSyncManager,
        private val customScheduleSourceSyncManager: CustomScheduleSourceSyncManager,
        private val todoSourceSyncManager: TodoSourceSyncManager,
        private val homeworkSourceSyncManager: HomeworkSourceSyncManager,
        private val goalsSourceSyncManager: DailyGoalsSourceSyncManager,
        private val employeeSourceSyncManager: EmployeeSourceSyncManager,
        private val organizationsSourceSyncManager: OrganizationsSourceSyncManager,
        private val subjectsSourceSyncManager: SubjectsSourceSyncManager,
        private val sharedSchedulesSourceSyncManager: SharedSchedulesSourceSyncManager,
        private val sharedHomeworksSourceSyncManager: SharedHomeworksSourceSyncManager,
        private val friendRequestsSourceSyncManager: FriendRequestsSourceSyncManager,
    ) : SourceSyncFacade {

        override suspend fun startAllSourceSync() {
            currentUserSourceSyncManager.startSourceSync()
            baseScheduleSourceSyncManager.startSourceSync()
            customScheduleSourceSyncManager.startSourceSync()
            todoSourceSyncManager.startSourceSync()
            homeworkSourceSyncManager.startSourceSync()
            goalsSourceSyncManager.startSourceSync()
            employeeSourceSyncManager.startSourceSync()
            organizationsSourceSyncManager.startSourceSync()
            subjectsSourceSyncManager.startSourceSync()
            sharedSchedulesSourceSyncManager.startSourceSync()
            sharedHomeworksSourceSyncManager.startSourceSync()
            friendRequestsSourceSyncManager.startSourceSync()
        }

        override fun stopAllSourceSync() {
            currentUserSourceSyncManager.stopSourceSync()
            baseScheduleSourceSyncManager.stopSourceSync()
            customScheduleSourceSyncManager.stopSourceSync()
            todoSourceSyncManager.stopSourceSync()
            homeworkSourceSyncManager.stopSourceSync()
            goalsSourceSyncManager.stopSourceSync()
            employeeSourceSyncManager.stopSourceSync()
            organizationsSourceSyncManager.stopSourceSync()
            subjectsSourceSyncManager.stopSourceSync()
            sharedSchedulesSourceSyncManager.stopSourceSync()
            sharedHomeworksSourceSyncManager.stopSourceSync()
            friendRequestsSourceSyncManager.stopSourceSync()
        }

        override suspend fun clearAllSyncedData() {
            currentUserSourceSyncManager.clearSourceData()
            baseScheduleSourceSyncManager.clearSourceData()
            customScheduleSourceSyncManager.clearSourceData()
            todoSourceSyncManager.clearSourceData()
            homeworkSourceSyncManager.clearSourceData()
            goalsSourceSyncManager.clearSourceData()
            employeeSourceSyncManager.clearSourceData()
            organizationsSourceSyncManager.clearSourceData()
            subjectsSourceSyncManager.clearSourceData()
            sharedSchedulesSourceSyncManager.clearSourceData()
            sharedHomeworksSourceSyncManager.clearSourceData()
            friendRequestsSourceSyncManager.clearSourceData()
        }
    }
}