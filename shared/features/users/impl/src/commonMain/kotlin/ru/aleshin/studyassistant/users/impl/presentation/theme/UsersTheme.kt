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

package ru.aleshin.studyassistant.users.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.users.impl.presentation.theme.tokens.LocalUsersIcons
import ru.aleshin.studyassistant.users.impl.presentation.theme.tokens.LocalUsersStrings
import ru.aleshin.studyassistant.users.impl.presentation.theme.tokens.fetchUsersIcons
import ru.aleshin.studyassistant.users.impl.presentation.theme.tokens.fetchUsersStrings

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
@Composable
internal fun UsersTheme(
    content: @Composable () -> Unit,
) {
    val icons = fetchUsersIcons(StudyAssistantRes.colors.isDark)
    val strings = fetchUsersStrings(StudyAssistantRes.language)

    CompositionLocalProvider(
        LocalUsersIcons provides icons,
        LocalUsersStrings provides strings,
        content = content,
    )
}