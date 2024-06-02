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

package extensions

import kotlinx.datetime.Clock
import kotlin.math.abs
import kotlin.random.Random

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
fun generateSevenDigitCode(): String {
    return abs(Random(Clock.System.now().toEpochMilliseconds()).nextInt()).toString().substring(IntRange(0, 6))
}

inline fun <T> Iterable<T>.forEachWith(action: T.() -> Unit) {
    for (element in this) action(element)
}