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

package ru.aleshin.studyassistant.widget.presentation.ui.goals

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.domain.entities.goal.WidgetGoalStatus
import ru.aleshin.studyassistant.widget.presentation.models.GoalWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.theme.compatCornerBackground
import ru.aleshin.studyassistant.widget.presentation.theme.formatWidgetDuration
import ru.aleshin.studyassistant.widget.presentation.theme.tintedSubjectColor
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.widgetAccents
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
@Composable
fun GoalWidgetRow(
    goal: GoalWidgetItemUi,
    sizeClass: WidgetSizeClass,
    action: Action,
) {
    val compact = sizeClass.width == WidgetSizeClass.Width.COMPACT ||
        sizeClass.height == WidgetSizeClass.Height.COMPACT
    val padding = if (compact) WidgetDimensions.spacingExtraSmall else WidgetDimensions.spacingSmall
    val statusColor = goal.status.statusColor()
    val goalColor = goal.color?.let { ColorProvider(Color(it)) } ?: GlanceTheme.colors.primary
    val container = goal.color?.let { tintedSubjectColor(it) } ?: GlanceTheme.colors.primaryContainer

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(WidgetDimensions.goalRowHeight)
            .compatCornerBackground(container, WidgetShapes.LARGE)
            .clickable(action)
            .padding(padding),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(if (compact) WidgetDimensions.icon else WidgetDimensions.touchTarget)
                    .compatCornerBackground(goalColor, WidgetShapes.FULL),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = goal.number.toString(),
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().label.copy(
                        color = ColorProvider(Color.White),
                    ),
                )
            }
            Spacer(GlanceModifier.width(WidgetDimensions.spacingSmall))
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    text = goal.title ?: goal.contentType.fallbackTitle(),
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().label.copy(
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
                Text(
                    text = goal.valueTitle(),
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().caption.copy(
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
            Text(
                text = "${(goal.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                maxLines = 1,
                style = GlanceTheme.widgetTypography().label.copy(color = statusColor),
            )
        }
        Spacer(GlanceModifier.height(WidgetDimensions.spacingExtraSmall))
        LinearProgressIndicator(
            progress = goal.progress.coerceIn(0f, 1f),
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(
                    if (compact) {
                        WidgetDimensions.progressHeightCompact
                    } else {
                        WidgetDimensions.progressHeight
                    },
                ),
            color = statusColor,
            backgroundColor = GlanceTheme.colors.surfaceVariant,
        )
    }
}

@Composable
private fun GoalWidgetItemUi.valueTitle(): String = when {
    timeType == GoalTime.Type.NONE -> widgetString(R.string.widget_goal_without_timer)
    targetTime != null -> "${formatWidgetDuration(elapsedTime)} / ${formatWidgetDuration(targetTime)}"
    else -> formatWidgetDuration(elapsedTime)
}

@Composable
private fun GoalType.fallbackTitle(): String = widgetString(
    when (this) {
        GoalType.HOMEWORK -> R.string.widget_goal_homework
        GoalType.TODO -> R.string.widget_goal_todo
    }
)

@Composable
private fun WidgetGoalStatus.statusColor(): ColorProvider {
    val accents = widgetAccents()
    return when (this) {
        WidgetGoalStatus.ACTIVE -> GlanceTheme.colors.primary
        WidgetGoalStatus.ACHIEVED -> accents.green
        WidgetGoalStatus.COMPLETED -> accents.orange
    }
}
