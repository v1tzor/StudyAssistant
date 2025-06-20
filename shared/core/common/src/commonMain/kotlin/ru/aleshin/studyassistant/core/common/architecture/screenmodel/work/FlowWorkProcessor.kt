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
package ru.aleshin.studyassistant.core.common.architecture.screenmodel.work

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.functional.DomainFailures
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.handleAndGet

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface FlowWorkProcessor<C : WorkCommand, A : BaseAction, E : BaseUiEffect> {

    suspend fun work(command: C): FlowWorkResult<A, E>

    @ExperimentalCoroutinesApi
    fun <L : DomainFailures, D1, D2> Flow<Either<L, D1>>.flatMapLatestWithResult(
        secondFlow: Flow<Either<L, D2>>,
        onError: suspend (L) -> E,
        onData: suspend (D1, D2) -> A,
    ): FlowWorkResult<A, E> {
        return flatMapLatest { eitherFirst ->
            eitherFirst.handleAndGet(
                onLeftAction = { flowOf(EffectResult(onError(it))) },
                onRightAction = { dataFirst ->
                    secondFlow.map { eitherSecond ->
                        eitherSecond.handleAndGet(
                            onLeftAction = { EffectResult(onError(it)) },
                            onRightAction = { dataSecond ->
                                ActionResult(onData(dataFirst, dataSecond))
                            },
                        )
                    }
                }
            )
        }
    }

    @ExperimentalCoroutinesApi
    fun <L : DomainFailures, D1, D2> Flow<Either<L, D1>>.combineWithResult(
        secondFlow: Flow<Either<L, D2>>,
        onError: suspend (L) -> E,
        onData: suspend (D1, D2) -> A,
    ): FlowWorkResult<A, E> {
        return combine(secondFlow) { eitherFirst, eitherSecond ->
            eitherFirst.handleAndGet(
                onLeftAction = { EffectResult(onError(it)) },
                onRightAction = { dataFirst ->
                    eitherSecond.handleAndGet(
                        onLeftAction = { EffectResult(onError(it)) },
                        onRightAction = { dataSecond ->
                            ActionResult(onData(dataFirst, dataSecond))
                        },
                    )
                }
            )
        }
    }
}

typealias FlowWorkResult<A, E> = Flow<WorkResult<A, E>>