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
import ru.aleshin.studyassistant.widget.presentation.theme.tintedSubjectColor
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.widgetAccents
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetStatusChip
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
    val palette = homework.status.colorPalette()
    val subjectColor = homework.subjectColor?.let { ColorProvider(Color(it)) } ?: palette.accent
    val background = homework.subjectColor?.let { tintedSubjectColor(it) } ?: palette.container
    val contentColor = if (homework.subjectColor != null) {
        GlanceTheme.colors.onSurface
    } else {
        palette.onContainer
    }

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
                    style = GlanceTheme.widgetTypography().label.copy(color = contentColor),
                )
                if (!compact) {
                    Spacer(GlanceModifier.width(WidgetDimensions.spacingExtraSmall))
                    WidgetStatusChip(
                        title = homeworkStatusTitle(homework.status),
                        container = palette.accent,
                        content = if (homework.status == HomeworkStatus.SKIPPED) {
                            palette.onContainer
                        } else {
                            ColorProvider(Color.White)
                        },
                    )
                } else {
                    Image(
                        modifier = GlanceModifier.size(WidgetDimensions.iconSmall),
                        provider = ImageProvider(homework.status.iconResource()),
                        contentDescription = homeworkStatusTitle(homework.status),
                        colorFilter = ColorFilter.tint(palette.accent),
                    )
                }
            }
            Spacer(GlanceModifier.height(WidgetDimensions.spacingExtraSmall))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HomeworkStat(
                    icon = R.drawable.ic_widget_book,
                    count = homework.theoreticalTasksCount,
                    description = widgetString(R.string.widget_theory_description),
                    tint = contentColor,
                )
                Spacer(GlanceModifier.width(WidgetDimensions.spacingSmall))
                HomeworkStat(
                    icon = R.drawable.ic_widget_tasks,
                    count = homework.practicalTasksCount,
                    description = widgetString(R.string.widget_practice_description),
                    tint = contentColor,
                )
                Spacer(GlanceModifier.width(WidgetDimensions.spacingSmall))
                HomeworkStat(
                    icon = R.drawable.ic_widget_presentation,
                    count = homework.presentationTasksCount,
                    description = widgetString(R.string.widget_presentation_description),
                    tint = contentColor,
                )
            }
        }
    }
}

@Composable
private fun HomeworkStat(
    icon: Int,
    count: Int,
    description: String,
    tint: ColorProvider,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            modifier = GlanceModifier.size(WidgetDimensions.iconSmall),
            provider = ImageProvider(icon),
            contentDescription = description,
            colorFilter = ColorFilter.tint(tint),
        )
        Spacer(GlanceModifier.width(WidgetDimensions.spacingExtraSmall))
        Text(
            text = count.toString(),
            maxLines = 1,
            style = GlanceTheme.widgetTypography().body.copy(color = tint),
        )
    }
}

private fun HomeworkStatus.iconResource(): Int = when (this) {
    HomeworkStatus.COMPLETE -> R.drawable.ic_widget_check
    HomeworkStatus.WAIT, HomeworkStatus.IN_FUTURE -> R.drawable.ic_widget_clock
    HomeworkStatus.SKIPPED, HomeworkStatus.NOT_COMPLETE -> R.drawable.ic_widget_error
}

@Composable
private fun HomeworkStatus.colorPalette(): HomeworkStatusPalette {
    val accents = widgetAccents()
    return when (this) {
        HomeworkStatus.COMPLETE -> HomeworkStatusPalette(
            accent = accents.green,
            container = accents.greenContainer,
            onContainer = accents.onGreenContainer,
        )
        HomeworkStatus.WAIT -> HomeworkStatusPalette(
            accent = accents.orange,
            container = accents.orangeContainer,
            onContainer = accents.onOrangeContainer,
        )
        HomeworkStatus.IN_FUTURE -> HomeworkStatusPalette(
            accent = GlanceTheme.colors.primary,
            container = GlanceTheme.colors.primaryContainer,
            onContainer = GlanceTheme.colors.onPrimaryContainer,
        )
        HomeworkStatus.NOT_COMPLETE -> HomeworkStatusPalette(
            accent = accents.red,
            container = accents.redContainer,
            onContainer = accents.onRedContainer,
        )
        HomeworkStatus.SKIPPED -> HomeworkStatusPalette(
            accent = accents.yellow,
            container = accents.yellowContainer,
            onContainer = accents.onYellowContainer,
        )
    }
}

private data class HomeworkStatusPalette(
    val accent: ColorProvider,
    val container: ColorProvider,
    val onContainer: ColorProvider,
)

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
