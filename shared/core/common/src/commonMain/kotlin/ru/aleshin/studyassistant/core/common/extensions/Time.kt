/*
 * Copyright 2023 Stanislav Aleshin
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
 * imitations under the License.
 */

package ru.aleshin.studyassistant.core.common.extensions

import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * @author Stanislav Aleshin on 08.08.2023.
 */
@ExperimentalTime
suspend fun <T> delayedAction(delayTime: Long, block: suspend () -> T): T {
    val measureResult = measureTimedValue { block() }
    val passedTime = measureResult.duration.toLong(DurationUnit.MILLISECONDS)
    if (passedTime < delayTime) delay(delayTime - passedTime)
    return measureResult.value
}