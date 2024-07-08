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

package ru.aleshin.studyassistant.auth.impl.presentation.validation

import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.PasswordValidError
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.Constants.Text.MIN_PASSWORD_LENGTH
import validation.ValidateResult
import validation.Validator

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
internal interface PasswordValidator : Validator<String, PasswordValidError> {
    class Base : PasswordValidator {
        override fun validate(data: String): ValidateResult<PasswordValidError> {
            return if (data.length >= MIN_PASSWORD_LENGTH && data.matches(Regex(Constants.Regex.PASSWORD))) {
                ValidateResult(true, null)
            } else {
                ValidateResult(false, PasswordValidError.FormatError)
            }
        }
    }
}