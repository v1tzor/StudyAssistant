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

package ru.aleshin.studyassistant.auth.impl.presentation.mappers

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.PasswordValidError
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes

/**
 * @author Stanislav Aleshin on 19.06.2023.
 */
@Composable
internal fun PasswordValidError.mapToMessage() = when (this) {
    is PasswordValidError.FormatError -> AuthThemeRes.strings.passwordFormatError
}