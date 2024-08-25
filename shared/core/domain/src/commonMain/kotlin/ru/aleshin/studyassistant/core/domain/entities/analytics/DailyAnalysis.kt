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

package ru.aleshin.studyassistant.core.domain.entities.analytics

import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
data class DailyAnalysis(
    val date: Instant,
    val generalAssessment: Float,
    val numberOfClasses: Int,
    val numberOfTests: Int,
    val numberOfMovements: Int,
    val homeworksProgress: List<Boolean>,
    val todosProgress: List<Boolean>,
) {
    companion object {
        const val MAX_RATE = 31f
        const val CLASS_MINUTE_DURATION_RATE = 1f / 60f
        const val TEST_RATE = 2f
        const val MOVEMENT_RATE = 2f
        const val THEORY_RATE = 0.2f
        const val PRACTICE_RATE = 0.4f
        const val PRESENTATION_RATE = 1.5f
        const val TODO_RATE = 1.0
        const val TODO_PRIORITY_RATE = 2.0
    }
}