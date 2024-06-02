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

package ru.aleshin.studyassistant.schedule.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens.LocalScheduleIcons
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens.LocalScheduleStrings
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens.fetchScheduleIcons
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens.fetchScheduleStrings
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 07.04.2024.
 */
@Composable
internal fun ScheduleTheme(content: @Composable () -> Unit) {
    val icons = fetchScheduleIcons(StudyAssistantRes.colors.isDark)
    val strings = fetchScheduleStrings(StudyAssistantRes.language)

    CompositionLocalProvider(
        LocalScheduleIcons provides icons,
        LocalScheduleStrings provides strings,
        content = content,
    )
}
