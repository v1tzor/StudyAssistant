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

import functional.Constants
import ru.aleshin.studyassistant.auth.impl.presentation.models.NicknameValidError
import validation.ValidateResult
import validation.Validator

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
internal interface NicknameValidator : Validator<String, NicknameValidError> {
    class Base : NicknameValidator {
        override fun validate(data: String): ValidateResult<NicknameValidError> {
            return if (data.length in 2..15 && data.matches(Regex(Constants.Regex.ONLY_TEXT))) {
                ValidateResult(true, null)
            } else {
                ValidateResult(false, NicknameValidError.LengthError)
            }
        }
    }
}
