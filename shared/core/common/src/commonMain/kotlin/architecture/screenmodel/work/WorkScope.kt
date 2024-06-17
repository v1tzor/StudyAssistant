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
package architecture.screenmodel.work

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import architecture.screenmodel.store.BaseStore
import functional.Either
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import managers.CoroutineBlock

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface WorkScope<S : BaseViewState, A : BaseAction, F : BaseUiEffect> : WorkResultHandler<A, F> {

    fun launchBackgroundWork(
        key: BackgroundWorkKey,
        dispatcher: CoroutineDispatcher? = null,
        scope: CoroutineScope? = null,
        block: CoroutineBlock,
    ): Job

    suspend fun state(): S
    suspend fun sendAction(action: A)
    suspend fun sendEffect(effect: F)

    class Base<S : BaseViewState, E : BaseEvent, A : BaseAction, F : BaseUiEffect>(
        private val store: BaseStore<S, E, A, F>,
        private val coroutineScope: CoroutineScope,
    ) : WorkScope<S, A, F> {

        private val backgroundWorkMap = mutableMapOf<BackgroundWorkKey, Job>()

        override suspend fun state(): S {
            return store.fetchState()
        }

        override suspend fun sendAction(action: A) {
            store.handleAction(action)
        }

        override suspend fun sendEffect(effect: F) {
            store.postEffect(effect)
        }

        override fun launchBackgroundWork(
            key: BackgroundWorkKey,
            dispatcher: CoroutineDispatcher?,
            scope: CoroutineScope?,
            block: CoroutineBlock,
        ): Job {
            backgroundWorkMap[key].let { job ->
                if (job != null) {
                    job.cancel()
                    backgroundWorkMap.remove(key)
                }
            }
            return (scope ?: coroutineScope).launch {
                dispatcher?.let { withContext(it, block) } ?: block()
            }.apply {
                backgroundWorkMap[key] = this
                start()
            }
        }

        override suspend fun Either<A, F>.handleWork() = when (this) {
            is Either.Left -> sendAction(data)
            is Either.Right -> sendEffect(data)
        }

        override suspend fun Flow<Either<A, F>>.collectAndHandleWork() = collect { it.handleWork() }
    }
}

interface BackgroundWorkKey

object MainWorkKey : BackgroundWorkKey