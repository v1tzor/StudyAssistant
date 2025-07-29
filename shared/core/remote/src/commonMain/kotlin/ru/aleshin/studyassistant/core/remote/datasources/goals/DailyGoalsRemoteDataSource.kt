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

package ru.aleshin.studyassistant.core.remote.datasources.goals

import ru.aleshin.studyassistant.core.api.AppwriteApi.Goals
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.remote.models.goals.GoalPojo
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 19.04.2025.
 */
interface DailyGoalsRemoteDataSource : RemoteDataSource.FullSynced.MultipleDocuments<GoalPojo> {

    class Base(
        database: DatabaseService,
        realtime: RealtimeService,
        dateManager: DateManager,
        userSessionProvider: UserSessionProvider
    ) : DailyGoalsRemoteDataSource, RemoteDataSource.FullSynced.MultipleDocuments.BaseAppwrite<GoalPojo>(
        database = database,
        realtime = realtime,
        dateManager = dateManager,
        userSessionProvider = userSessionProvider,
    ) {

        override val collectionId = Goals.COLLECTION_ID

        override val callbackCollectionId = Goals.CALLBACK_COLLECTION_ID

        override val nestedType = GoalPojo.serializer()

        override fun permissions(currentUser: UID) = Permission.onlyUserData(currentUser)
    }
}