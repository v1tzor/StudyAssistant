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

package ru.aleshin.studyassistant.core.remote.ktor

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
object StudyAssistantKtor {

    object Backend {
        const val INSTALLATION_REGISTER = "/api/v1/installations/register"
        const val AI_COMPLETIONS = "/api/v1/ai/completions"
        const val SCHEDULE_EXTRACTIONS = "/api/v1/ai/schedule-extractions"
        const val AD_REWARD_CHALLENGES = "/api/v1/ad-rewards/challenges"
        const val SCHEDULE_SHARE_CREATE = "/api/v1/shares/schedule/create"
        const val SCHEDULE_SHARE_CLAIM = "/api/v1/shares/schedule/claim"
        const val SCHEDULE_SHARE_CONFIRM = "/api/v1/shares/schedule/confirm"
        const val SCHEDULE_SHARE_RELEASE = "/api/v1/shares/schedule/release"
        const val HOMEWORK_SHARE_CREATE = "/api/v1/shares/homework/create"
        const val HOMEWORK_SHARE_FETCH = "/api/v1/shares/homework/fetch"
        const val INSTALLATION_TOKEN_HEADER = "X-Installation-Token"
    }
}
