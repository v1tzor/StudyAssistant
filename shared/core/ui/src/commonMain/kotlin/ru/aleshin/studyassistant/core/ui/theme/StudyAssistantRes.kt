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
package ru.aleshin.studyassistant.core.ui.theme

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalStudyAssistantColors
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalStudyAssistantElevations
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalStudyAssistantIcons
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalStudyAssistantLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalStudyAssistantStrings
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantColors
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantElevations
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantIcons
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 13.04.2023.
 */
object StudyAssistantRes {

    val elevations: StudyAssistantElevations
        @Composable get() = LocalStudyAssistantElevations.current

    val language: StudyAssistantLanguage
        @Composable get() = LocalStudyAssistantLanguage.current

    val colors: StudyAssistantColors
        @Composable get() = LocalStudyAssistantColors.current

    val strings: StudyAssistantStrings
        @Composable get() = LocalStudyAssistantStrings.current

    val icons: StudyAssistantIcons
        @Composable get() = LocalStudyAssistantIcons.current
}