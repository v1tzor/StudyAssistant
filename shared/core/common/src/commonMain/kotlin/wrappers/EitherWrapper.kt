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
package wrappers

import functional.DomainFailures
import functional.Either
import handlers.ErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface EitherWrapper<F : DomainFailures> {

    suspend fun <O> wrap(block: suspend () -> O): Either<F, O>

    suspend fun wrapUnit(block: suspend () -> Unit): Either<F, Unit>

    abstract class Abstract<F : DomainFailures>(
        private val errorHandler: ErrorHandler<F>,
    ) : EitherWrapper<F> {

        override suspend fun <O> wrap(block: suspend () -> O) = try {
            Either.Right(data = block.invoke())
        } catch (error: Throwable) {
            Either.Left(data = errorHandler.handle(error))
        }

        override suspend fun wrapUnit(block: suspend () -> Unit) = wrap(block)
    }
}

interface FlowEitherWrapper<F : DomainFailures> : EitherWrapper<F> {

    suspend fun <O> wrapFlow(block: suspend () -> Flow<O>): Flow<Either<F, O>>

    abstract class Abstract<F : DomainFailures>(
        private val errorHandler: ErrorHandler<F>,
    ) : FlowEitherWrapper<F>, EitherWrapper.Abstract<F>(errorHandler) {

        override suspend fun <O> wrapFlow(block: suspend () -> Flow<O>) = flow {
            block.invoke().catch { error ->
                this@flow.emit(Either.Left(data = errorHandler.handle(error)))
            }.collect { data ->
                emit(Either.Right(data = data))
            }
        }
    }
}