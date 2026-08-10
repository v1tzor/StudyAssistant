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

package ru.aleshin.studyassistant.presentation.mappers

import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.domain.entities.MainFailures
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.network_error_message as core_network_error_message
import ru.aleshin.studyassistant.core.ui.resources.other_error_message as core_other_error_message

/**
 * @author Stanislav Aleshin on 13.09.2024.
 */
suspend fun MainFailures.mapToMessage() = when (this) {
    is MainFailures.NetworkError -> getString(CoreRes.string.core_network_error_message)
    is MainFailures.OtherError -> getString(CoreRes.string.core_other_error_message)
}
