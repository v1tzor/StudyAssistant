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
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.Text
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.presentation.theme.compatCornerBackground
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
fun WidgetHeader(
    title: String,
    titleAction: Action,
    actionIcon: ImageProvider,
    actionDescription: String,
    action: Action,
    secondaryActionIcon: ImageProvider,
    secondaryActionDescription: String,
    secondaryAction: Action,
) {
    val sizeClass = WidgetSizeClass.fetch(LocalSize.current)
    val isCompact = sizeClass.width == WidgetSizeClass.Width.COMPACT
    val showSecondaryAction = sizeClass.width == WidgetSizeClass.Width.EXPANDED
    val iconSize = if (isCompact) WidgetDimensions.iconCompact else WidgetDimensions.icon

    Row(
        modifier = GlanceModifier.fillMaxWidth().height(WidgetDimensions.headerHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier.size(WidgetDimensions.touchTarget).clickable(titleAction),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = GlanceModifier.size(iconSize),
                provider = ImageProvider(R.drawable.ic_widget_logo),
                contentDescription = null,
            )
        }
        if (!isCompact) {
            Text(
                modifier = GlanceModifier.clickable(titleAction),
                text = title,
                maxLines = 1,
                style = GlanceTheme.widgetTypography().title.copy(
                    color = GlanceTheme.colors.onBackground,
                ),
            )
        }
        Spacer(GlanceModifier.defaultWeight())
        if (showSecondaryAction) {
            WidgetHeaderAction(
                icon = secondaryActionIcon,
                description = secondaryActionDescription,
                action = secondaryAction,
                iconSize = iconSize,
            )
        }
        WidgetHeaderAction(
            icon = actionIcon,
            description = actionDescription,
            action = action,
            iconSize = iconSize,
        )
    }
}

@Composable
private fun WidgetHeaderAction(
    icon: ImageProvider,
    description: String,
    action: Action,
    iconSize: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = GlanceModifier
            .size(WidgetDimensions.touchTarget)
            .clickable(action),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier
                .size(WidgetDimensions.headerActionContainer)
                .compatCornerBackground(
                    GlanceTheme.colors.secondaryContainer,
                    WidgetShapes.FULL,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = GlanceModifier.size(iconSize),
                provider = icon,
                contentDescription = description,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer),
            )
        }
    }
}
