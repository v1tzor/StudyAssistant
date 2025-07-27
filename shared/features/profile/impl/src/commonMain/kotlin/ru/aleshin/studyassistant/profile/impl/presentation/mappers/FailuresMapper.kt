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

package ru.aleshin.studyassistant.profile.impl.presentation.mappers

import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures
import ru.aleshin.studyassistant.profile.impl.presentation.theme.tokens.ProfileStrings

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal fun ProfileFailures.mapToMessage(
    strings: ProfileStrings,
    coreStrings: StudyAssistantStrings,
) = when (this) {
    is ProfileFailures.InternetError -> coreStrings.networkErrorMessage
    is ProfileFailures.OtherError -> strings.otherErrorMessage
}