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

package ru.aleshin.studyassistant.core.common.extensions

/**
 * @author Stanislav Aleshin on 25.05.2024.
 */
fun max(a: Long?, b: Long?): Long? {
    return when {
        a != null && b != null -> kotlin.math.max(a, b)
        a == null && b != null -> b
        a != null && b == null -> a
        else -> null
    }
}

fun min(a: Long?, b: Long?): Long? {
    return when {
        a != null && b != null -> kotlin.math.min(a, b)
        a == null && b != null -> b
        a != null && b == null -> a
        else -> null
    }
}