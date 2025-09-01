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
package ru.aleshin.studyassistant.core.common.managers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface CoroutineManager : WorkDispatchersProvider {

    fun runOnIOBackground(scope: CoroutineScope, block: CoroutineBlock): Job

    fun runOnDefaultBackground(scope: CoroutineScope, block: CoroutineBlock): Job

    fun runOnUi(scope: CoroutineScope, block: CoroutineBlock): Job

    suspend fun <T> changeFlow(coroutineFlow: CoroutineFlow, block: suspend CoroutineScope.() -> T): T

    abstract class Abstract(
        override val ioDispatcher: CoroutineDispatcher,
        override val defaultDispatcher: CoroutineDispatcher,
        override val uiDispatcher: CoroutineDispatcher,
    ) : CoroutineManager {

        override fun runOnIOBackground(scope: CoroutineScope, block: CoroutineBlock): Job {
            return scope.launch(context = ioDispatcher, block = block)
        }

        override fun runOnDefaultBackground(scope: CoroutineScope, block: CoroutineBlock): Job {
            return scope.launch(context = defaultDispatcher, block = block)
        }

        override fun runOnUi(scope: CoroutineScope, block: CoroutineBlock): Job {
            return scope.launch(context = uiDispatcher, block = block)
        }

        override suspend fun <T> changeFlow(
            coroutineFlow: CoroutineFlow,
            block: suspend CoroutineScope.() -> T
        ): T {
            val dispatcher = when (coroutineFlow) {
                CoroutineFlow.IO -> ioDispatcher
                CoroutineFlow.DEFAULT -> defaultDispatcher
                CoroutineFlow.UI -> uiDispatcher
            }
            return withContext(context = dispatcher, block = block)
        }
    }

    class Base : Abstract(
        ioDispatcher = Dispatchers.IO,
        defaultDispatcher = Dispatchers.Default,
        uiDispatcher = Dispatchers.Main,
    )
}

interface WorkDispatchersProvider {
    val ioDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
    val uiDispatcher: CoroutineDispatcher
}

typealias CoroutineBlock = suspend CoroutineScope.() -> Unit

enum class CoroutineFlow {
    IO, DEFAULT, UI
}