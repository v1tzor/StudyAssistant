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

package ru.aleshin.studyassistant.chat.impl.presentation.models.orgnizations

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.max
import ru.aleshin.studyassistant.core.common.extensions.min
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class ScheduleTimeIntervalsUi(
    @TypeParceler<Instant?, NullInstantParceler>
    val firstClassTime: Instant? = null,
    val baseClassDuration: Millis? = null,
    val baseBreakDuration: Millis? = null,
    val specificClassDuration: List<NumberedDurationUi> = emptyList(),
    val specificBreakDuration: List<NumberedDurationUi> = emptyList(),
) : Parcelable {

    fun minClassDuration(): Millis? {
        val specificMin = specificClassDuration.minOfOrNull { it.duration }
        val baseMin = baseClassDuration
        return min(specificMin, baseMin)
    }

    fun minBreakDuration(): Millis? {
        val specificMin = specificBreakDuration.minOfOrNull { it.duration }
        val baseMin = baseBreakDuration
        return min(specificMin, baseMin)
    }

    fun maxClassDuration(): Millis? {
        val specificMax = specificClassDuration.maxOfOrNull { it.duration }
        val baseMax = baseClassDuration
        return max(specificMax, baseMax)
    }

    fun maxBreakDuration(): Millis? {
        val specificMax = specificBreakDuration.maxOfOrNull { it.duration }
        val baseMax = baseBreakDuration
        return max(specificMax, baseMax)
    }

    fun averageClassDuration(): Millis? {
        return ((maxClassDuration() ?: return null) + (minClassDuration() ?: return null)) / 2
    }

    fun averageBreakDuration(): Millis? {
        return ((maxBreakDuration() ?: return null) + (minBreakDuration() ?: return null)) / 2
    }

    fun averageBreakClassDuration(): Millis? {
        return ((averageClassDuration() ?: return null) + (averageBreakDuration() ?: return null)) / 2
    }
}