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

package ru.aleshin.studyassistant.backend.maintenance

import kotlinx.coroutines.runBlocking
import ru.aleshin.studyassistant.backend.ai.infrastructure.AiCleanupRepositoryImpl
import ru.aleshin.studyassistant.backend.ai.services.AiCleanupService
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.sharing.infrastructure.SharingCleanupRepositoryImpl
import ru.aleshin.studyassistant.backend.sharing.services.SharingCleanupService
import java.time.Clock

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun main() = runBlocking {
    val config = CleanupConfig.fromEnvironment()

    val databaseFactory = DatabaseFactory(
        config = config.database,
    )

    databaseFactory.use { databaseFactory ->
        val repository = SharingCleanupRepositoryImpl(database = databaseFactory.database)

        val service = SharingCleanupService(
            repository = repository,
            clock = Clock.systemUTC(),
        )

        val result = service.cleanup()
        val aiResult = AiCleanupService(
            repository = AiCleanupRepositoryImpl(database = databaseFactory.database),
            clock = Clock.systemUTC(),
        ).cleanup()

        println(
            "Sharing cleanup completed: " +
            "scheduleShares=${result.removedScheduleShares}, " +
            "homeworkShares=${result.removedHomeworkShares}, " +
            "rateLimitEvents=${result.removedRateLimitEvents}, " +
            "aiRequests=${aiResult.removedRequests}, " +
            "aiUsageRows=${aiResult.removedUsageRows}, " +
            "adRewardChallenges=${aiResult.removedRewardChallenges}",
        )
    }
}
