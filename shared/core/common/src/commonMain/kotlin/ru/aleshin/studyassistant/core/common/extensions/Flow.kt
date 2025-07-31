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

import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
import kotlinx.io.IOException

/**
 * Retries the execution of the Flow when an [IOException] is thrown,
 * and waits for an active internet connection before resubscribing.
 *
 * This is useful for network-bound flows that should automatically retry
 * after connectivity is restored.
 *
 * Example usage:
 * ```
 * flow { emit(fetchFromNetwork()) }.retryOnReconnect(connectionManager)
 * ```
 *
 * @param connectionManager An instance that observes internet connectivity state.
 * @return A Flow that automatically retries after reconnecting to the internet.
 *
 * @author Stanislav Aleshin on 31.07.2025.
 */

fun <T> Flow<T>.retryOnReconnect(connectionManager: Konnection) = retryWhen { exception, _ ->
    if (exception is IOException) {
        connectionManager.observeHasConnection().first { it }
        true
    } else {
        false
    }
}

fun <T> Flow<T>.catchIOException(
    action: suspend FlowCollector<T>.(IOException) -> Unit = {},
) = catch { exception ->
    if (exception is IOException) action(exception) else throw exception
}