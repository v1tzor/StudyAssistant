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

package ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class ScheduleStrings(
    val overviewHeader: String,
    val detailsHeader: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = ScheduleStrings(
            overviewHeader = "Обзор",
            detailsHeader = "Расписание",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = ScheduleStrings(
            overviewHeader = "Overview",
            detailsHeader = "Schedule",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalScheduleStrings = staticCompositionLocalOf<ScheduleStrings> {
    error("Schedule Strings is not provided")
}

internal fun fetchScheduleStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> ScheduleStrings.ENGLISH
    StudyAssistantLanguage.RU -> ScheduleStrings.RUSSIAN
}