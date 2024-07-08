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

package ru.aleshin.studyassistant.auth.impl.presentation.mappers

import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.ForgotCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.RegisterCredentialsUi
import ru.aleshin.studyassistant.core.domain.entities.auth.AuthCredentials
import ru.aleshin.studyassistant.core.domain.entities.auth.ForgotCredentials

/**
 * @author Stanislav Aleshin on 10.06.2024.
 */
internal fun LoginCredentialsUi.mapToDomain() =
    AuthCredentials(
        username = null,
        email = email,
        password = password,
    )

internal fun RegisterCredentialsUi.mapToDomain() =
    AuthCredentials(
        username = username,
        email = email,
        password = password,
    )

internal fun ForgotCredentialsUi.mapToDomain() =
    ForgotCredentials(
        email = email,
    )