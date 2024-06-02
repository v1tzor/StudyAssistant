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

package entities.organizations

import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
data class ScheduleTimeIntervals(
    val firstClassTime: Instant? = null,
    val baseClassDuration: Millis? = null,
    val baseBreakDuration: Millis? = null,
    val specificClassDuration: List<NumberedDuration> = emptyList(),
    val specificBreakDuration: List<NumberedDuration> = emptyList(),
)
