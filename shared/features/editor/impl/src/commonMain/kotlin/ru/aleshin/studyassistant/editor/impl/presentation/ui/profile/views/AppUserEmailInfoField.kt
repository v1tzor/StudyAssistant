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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun AppUserEmailInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    email: String,
) {
    InfoTextField(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        readOnly = true,
        value = email,
        onValueChange = {},
        label = EditorThemeRes.strings.emailFieldLabel,
        leadingInfoIcon = painterResource(StudyAssistantRes.icons.email),
    )
}