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

package ru.aleshin.studyassistant.editor.impl.presentation.mappers

import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.EditorStrings

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal fun EditorFailures.mapToMessage(
    strings: EditorStrings,
    coreString: StudyAssistantStrings,
) = when (this) {
    is EditorFailures.CredentialsError -> strings.credentialsErrorMessage
    is EditorFailures.ShiftTimeError -> strings.shiftTimeError
    is EditorFailures.InternetError -> coreString.networkErrorMessage
    is EditorFailures.OtherError -> strings.otherErrorMessage
}