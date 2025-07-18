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

package ru.aleshin.studyassistant.settings.impl.presentation.mappers

import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.SettingsStrings

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal fun SettingsFailures.mapToMessage(
    strings: SettingsStrings,
    coreStrings: StudyAssistantStrings
) = when (this) {
    is SettingsFailures.IapError -> type.mapToString(coreStrings)
    is SettingsFailures.RestoreError -> strings.failureRestoreSubscriptionTitle
    is SettingsFailures.OtherError -> strings.otherErrorMessage
}