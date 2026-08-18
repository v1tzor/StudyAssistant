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
package ru.aleshin.studyassistant.core.common.managers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.epochTimeDuration
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.toMinutes
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface DateManager {

    fun fetchCurrentInstant(): Instant
    fun fetchCurrentWeek(): TimeRange
    fun fetchBeginningCurrentInstant(): Instant
    fun fetchEndCurrentInstant(): Instant
    fun isCurrentDay(date: Instant): Boolean
    fun calculateLeftDateTime(endDateTime: Instant): Long
    fun calculateLeftTime(endTime: LocalTime): Long
    fun calculateProgress(startTime: Instant, endTime: Instant): Float
    fun secondTicker(): Flow<Unit>
    fun minuteTicker(): Flow<Instant>

    class Base(
        private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
        workDispatchersProvider: WorkDispatchersProvider,
    ) : DateManager {

        private val ticker = flow {
            while (true) {
                emit(Unit)
                delay(1000)
            }
        }.flowOn(workDispatchersProvider.defaultDispatcher)

        private val minuteTicker = flow {
            while (true) {
                val currentDate = fetchCurrentInstant()
                emit(currentDate)

                val passedMinuteTime = floorMod(currentDate.toEpochMilliseconds(), Constants.Date.MILLIS_IN_MINUTE)
                delay((Constants.Date.MILLIS_IN_MINUTE - passedMinuteTime).milliseconds)
            }
        }.flowOn(workDispatchersProvider.defaultDispatcher)

        override fun fetchCurrentInstant() = Clock.System.now()

        override fun fetchCurrentWeek(): TimeRange {
            return fetchCurrentInstant().toLocalDateTime(timeZone).weekTimeRange(timeZone)
        }

        override fun fetchBeginningCurrentInstant(): Instant {
            return fetchCurrentInstant().startThisDay(timeZone)
        }

        override fun fetchEndCurrentInstant(): Instant {
            return fetchCurrentInstant().endThisDay(timeZone)
        }

        override fun isCurrentDay(date: Instant): Boolean {
            return fetchCurrentInstant().equalsDay(date, timeZone)
        }

        override fun calculateLeftDateTime(endDateTime: Instant): Long {
            val currentDate = fetchCurrentInstant()
            val duration = endDateTime.toEpochMilliseconds() - currentDate.toEpochMilliseconds()
            return duration
        }

        override fun calculateLeftTime(endTime: LocalTime): Long {
            val currentTimeMillis = fetchCurrentInstant().dateTime().time.epochTimeDuration()
            val duration = endTime.epochTimeDuration() - currentTimeMillis
            return duration
        }

        override fun calculateProgress(startTime: Instant, endTime: Instant): Float {
            val currentTimeMillis = fetchCurrentInstant().toEpochMilliseconds()
            val pastTime = ((currentTimeMillis - startTime.toEpochMilliseconds()).toMinutes()).toFloat()
            val duration = ((endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()).toMinutes()).toFloat()
            val progress = pastTime / duration

            return if (progress < 0f) -1f else if (progress > 1f) 1f else progress
        }

        override fun secondTicker() = ticker

        override fun minuteTicker() = minuteTicker

        private fun floorMod(a: Long, b: Long): Long = ((a % b) + b) % b
    }
}
