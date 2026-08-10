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

package ru.aleshin.studyassistant.widget.presentation.ui.homeworks

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.presentation.models.HomeworkWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.theme.compatCornerBackground
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
@Composable
fun HomeworkWidgetRow(
    homework: HomeworkWidgetItemUi,
    sizeClass: WidgetSizeClass,
    action: Action,
) {
    val compact = sizeClass.width == WidgetSizeClass.Width.COMPACT ||
        sizeClass.height == WidgetSizeClass.Height.COMPACT
    val padding = if (compact) WidgetDimensions.spacingExtraSmall else WidgetDimensions.spacingSmall
    val subjectColor = homework.subjectColor?.let { ColorProvider(Color(it)) }
        ?: GlanceTheme.colors.outline
    val background = homework.subjectColor?.let {
        ColorProvider(Color(it).copy(alpha = 0.14f))
    } ?: GlanceTheme.colors.surfaceVariant

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(WidgetDimensions.homeworkRowHeight)
            .compatCornerBackground(background, WidgetShapes.LARGE)
            .clickable(action),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .width(WidgetDimensions.subjectRailWidth)
                .fillMaxHeight()
                .padding(vertical = WidgetDimensions.spacingSmall)
                .compatCornerBackground(subjectColor, WidgetShapes.FULL),
        ) {}
        Column(
            modifier = GlanceModifier.defaultWeight().padding(padding),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = GlanceModifier.defaultWeight(),
                    text = homework.subjectName ?: widgetString(R.string.widget_no_subject),
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().label.copy(
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
                Image(
                    modifier = GlanceModifier.size(WidgetDimensions.iconSmall),
                    provider = ImageProvider(homework.status.iconResource()),
                    contentDescription = homeworkStatusTitle(homework.status),
                    colorFilter = ColorFilter.tint(homework.status.statusColor()),
                )
            }
            Spacer(GlanceModifier.height(WidgetDimensions.spacingExtraSmall))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HomeworkStat(
                    icon = R.drawable.ic_widget_book,
                    count = homework.theoreticalTasksCount,
                    description = widgetString(R.string.widget_theory_description),
                )
                Spacer(GlanceModifier.width(WidgetDimensions.spacingSmall))
                HomeworkStat(
                    icon = R.drawable.ic_widget_tasks,
                    count = homework.practicalTasksCount,
                    description = widgetString(R.string.widget_practice_description),
                )
                Spacer(GlanceModifier.width(WidgetDimensions.spacingSmall))
                HomeworkStat(
                    icon = R.drawable.ic_widget_presentation,
                    count = homework.presentationTasksCount,
                    description = widgetString(R.string.widget_presentation_description),
                )
            }
        }
    }
}

@Composable
private fun HomeworkStat(icon: Int, count: Int, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            modifier = GlanceModifier.size(WidgetDimensions.iconSmall),
            provider = ImageProvider(icon),
            contentDescription = description,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
        )
        Spacer(GlanceModifier.width(WidgetDimensions.spacingExtraSmall))
        Text(
            text = count.toString(),
            maxLines = 1,
            style = GlanceTheme.widgetTypography().body.copy(
                color = GlanceTheme.colors.onSurface,
            ),
        )
    }
}

private fun HomeworkStatus.iconResource(): Int = when (this) {
    HomeworkStatus.COMPLETE -> R.drawable.ic_widget_check
    HomeworkStatus.WAIT, HomeworkStatus.IN_FUTURE -> R.drawable.ic_widget_clock
    HomeworkStatus.SKIPPED, HomeworkStatus.NOT_COMPLETE -> R.drawable.ic_widget_error
}

@Composable
private fun HomeworkStatus.statusColor(): ColorProvider = when (this) {
    HomeworkStatus.COMPLETE -> GlanceTheme.colors.tertiary
    HomeworkStatus.WAIT -> GlanceTheme.colors.secondary
    HomeworkStatus.IN_FUTURE -> GlanceTheme.colors.primary
    HomeworkStatus.SKIPPED, HomeworkStatus.NOT_COMPLETE -> GlanceTheme.colors.error
}

@Composable
private fun homeworkStatusTitle(status: HomeworkStatus): String = widgetString(
    when (status) {
        HomeworkStatus.COMPLETE -> R.string.widget_homework_complete
        HomeworkStatus.WAIT -> R.string.widget_homework_wait
        HomeworkStatus.IN_FUTURE -> R.string.widget_homework_future
        HomeworkStatus.SKIPPED -> R.string.widget_homework_skipped
        HomeworkStatus.NOT_COMPLETE -> R.string.widget_homework_overdue
    }
)
