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

package ru.aleshin.studyassistant.editor.impl.domain.common

import ru.aleshin.studyassistant.core.common.exceptions.AppwriteException
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.handlers.ErrorHandler
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.domain.entities.ShiftTimeError

/**
 * @author Stanislav Aleshin on 27.05.2024
 */
internal interface EditorErrorHandler : ErrorHandler<EditorFailures> {
    class Base : EditorErrorHandler {
        override fun handle(throwable: Throwable) = when (throwable) {
            is InternetConnectionException -> EditorFailures.InternetError
            is AppwriteException -> if (throwable.type == "user_invalid_credentials") {
                EditorFailures.CredentialsError
            } else {
                EditorFailures.OtherError(throwable)
            }
            is ShiftTimeError -> EditorFailures.ShiftTimeError
            else -> EditorFailures.OtherError(throwable)
        }
    }
}