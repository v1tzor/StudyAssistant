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
package ru.aleshin.studyassistant.core.common.validation

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
data class ValidateResult<E : ValidateError>(
    val isValid: Boolean,
    val validError: E?,
)

suspend fun <E : ValidateError> ValidateResult<E>.handle(
    onValid: suspend () -> Unit,
    onError: suspend (E) -> Unit,
) = when (this.isValid) {
    true -> onValid()
    false -> onError(checkNotNull(this.validError))
}

suspend fun operateValidate(
    isSuccess: suspend () -> Unit,
    isError: suspend () -> Unit,
    vararg isValid: Boolean,
) {
    when (isValid.contains(false)) {
        true -> isError()
        false -> isSuccess()
    }
}
