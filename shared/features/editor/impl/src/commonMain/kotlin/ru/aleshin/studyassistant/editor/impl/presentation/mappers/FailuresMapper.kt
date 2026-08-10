/*
 * Copyright 2026 Stanislav Aleshin
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

import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.credentials_error_message
import ru.aleshin.studyassistant.editor.impl.resources.other_error_message
import ru.aleshin.studyassistant.editor.impl.resources.shift_time_error
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.network_error_message as core_network_error_message

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal suspend fun EditorFailures.mapToMessage() = when (this) {
    is EditorFailures.CredentialsError -> getString(Res.string.credentials_error_message)
    is EditorFailures.ShiftTimeError -> getString(Res.string.shift_time_error)
    is EditorFailures.InternetError -> getString(CoreRes.string.core_network_error_message)
    is EditorFailures.OtherError -> getString(Res.string.other_error_message)
}