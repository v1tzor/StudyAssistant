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

package ru.aleshin.studyassistant.widget.presentation.ui.common

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.presentation.state.WidgetContentStatusUi
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
fun WidgetStateContent(
    status: WidgetContentStatusUi,
    isStale: Boolean,
    emptyTitle: String,
    content: @Composable () -> Unit,
) {
    when (status) {
        WidgetContentStatusUi.LOADING -> WidgetMessage(widgetString(R.string.widget_loading))
        WidgetContentStatusUi.EMPTY -> WidgetMessage(emptyTitle)
        WidgetContentStatusUi.ERROR -> WidgetMessage(widgetString(R.string.widget_error))
        WidgetContentStatusUi.CONTENT -> Column(GlanceModifier.fillMaxSize()) {
            if (isStale) {
                Text(
                    text = widgetString(R.string.widget_stale),
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().caption.copy(
                        color = GlanceTheme.colors.error,
                    ),
                )
            }
            Box(GlanceModifier.defaultWeight(), content = content)
        }
    }
}

@Composable
private fun WidgetMessage(title: String) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            maxLines = 2,
            style = GlanceTheme.widgetTypography().label.copy(
                color = GlanceTheme.colors.onSurfaceVariant,
            ),
        )
    }
}
