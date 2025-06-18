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

package ru.aleshin.studyassistant.billing.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ru.aleshin.studyassistant.billing.impl.presentation.theme.tokens.LocalBillingIcons
import ru.aleshin.studyassistant.billing.impl.presentation.theme.tokens.LocalBillingStrings
import ru.aleshin.studyassistant.billing.impl.presentation.theme.tokens.fetchBillingIcons
import ru.aleshin.studyassistant.billing.impl.presentation.theme.tokens.fetchBillingStrings
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
internal fun BillingTheme(content: @Composable () -> Unit) {
    val icons = fetchBillingIcons(StudyAssistantRes.colors.isDark)
    val strings = fetchBillingStrings(StudyAssistantRes.language)

    CompositionLocalProvider(
        LocalBillingIcons provides icons,
        LocalBillingStrings provides strings,
        content = content,
    )
}
