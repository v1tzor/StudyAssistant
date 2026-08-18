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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import ru.aleshin.studyassistant.widget.presentation.theme.compatCornerBackground
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
@Composable
fun WidgetStatusChip(
    title: String,
    container: ColorProvider,
    content: ColorProvider,
) {
    Box(
        modifier = GlanceModifier
            .height(WidgetDimensions.statusChipHeight)
            .compatCornerBackground(container, WidgetShapes.FULL)
            .padding(horizontal = WidgetDimensions.spacingSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            maxLines = 1,
            style = GlanceTheme.widgetTypography().caption.copy(color = content),
        )
    }
}
