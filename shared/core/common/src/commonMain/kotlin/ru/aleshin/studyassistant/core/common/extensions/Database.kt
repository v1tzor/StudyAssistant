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

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 24.07.2025.
 */
fun <I : Any, O> Query<I>.mapToListFlow(
    coroutineContext: CoroutineContext,
    mapper: (I) -> O,
): Flow<List<O>> {
    return this.asFlow().mapToList(coroutineContext).map { items ->
        items.map { mapper.invoke(it) }
    }
}

fun <I : Any, O> Query<I>.mapToOneFlow(
    coroutineContext: CoroutineContext,
    mapper: (I) -> O,
): Flow<O> {
    return this.asFlow().mapToOne(coroutineContext).map { item ->
        mapper.invoke(item)
    }
}

fun <I : Any, O> Query<I>.mapToOneOrNullFlow(
    coroutineContext: CoroutineContext,
    mapper: (I) -> O,
): Flow<O?> {
    return this.asFlow().mapToOneOrNull(coroutineContext).map { item ->
        item?.let { mapper.invoke(it) }
    }
}