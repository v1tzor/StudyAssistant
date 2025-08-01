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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import ru.aleshin.studyassistant.core.common.functional.AnySerializer
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
inline fun <reified T : Any> classOf(): KClass<T> {
    @Suppress("UNCHECKED_CAST")
    return (typeOf<T>().classifier!! as KClass<T>)
}

fun mapSerializer(): KSerializer<Map<String, Any>> {
    return MapSerializer(String.serializer(), AnySerializer)
}