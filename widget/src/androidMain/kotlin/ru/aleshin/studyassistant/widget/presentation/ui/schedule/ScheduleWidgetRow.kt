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

package ru.aleshin.studyassistant.widget.presentation.ui.schedule

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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.domain.entities.schedule.WidgetScheduleStatus
import ru.aleshin.studyassistant.widget.presentation.models.ScheduleWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.theme.compatCornerBackground
import ru.aleshin.studyassistant.widget.presentation.theme.formatWidgetTime
import ru.aleshin.studyassistant.widget.presentation.theme.tintedSubjectColor
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
@Composable
fun ScheduleWidgetRow(
    item: ScheduleWidgetItemUi,
    sizeClass: WidgetSizeClass,
    action: Action,
) {
    val compactWidth = sizeClass.width == WidgetSizeClass.Width.COMPACT
    val compactHeight = sizeClass.height == WidgetSizeClass.Height.COMPACT
    val spacing = if (compactWidth || compactHeight) {
        WidgetDimensions.spacingExtraSmall
    } else {
        WidgetDimensions.spacingSmall
    }
    val rowHeight = if (compactHeight) {
        WidgetDimensions.scheduleRowHeightCompact
    } else {
        WidgetDimensions.scheduleRowHeight
    }
    val timeWidth = if (compactWidth) {
        WidgetDimensions.timeColumnWidthCompact
    } else {
        WidgetDimensions.timeColumnWidth
    }
    val subjectColor = item.color?.let { ColorProvider(Color(it)) }
    val container = when (item.status) {
        WidgetScheduleStatus.ACTIVE -> GlanceTheme.colors.primaryContainer
        WidgetScheduleStatus.COMPLETED -> GlanceTheme.colors.surfaceVariant
        WidgetScheduleStatus.UPCOMING -> item.color?.let { color ->
            tintedSubjectColor(color)
        } ?: GlanceTheme.colors.primaryContainer
    }
    val titleColor = if (item.status == WidgetScheduleStatus.COMPLETED) {
        GlanceTheme.colors.onSurfaceVariant
    } else {
        GlanceTheme.colors.onSurface
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth().height(rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = GlanceModifier.width(timeWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatWidgetTime(item.start),
                maxLines = 1,
                style = GlanceTheme.widgetTypography().label.copy(
                    color = GlanceTheme.colors.onSurface,
                ),
            )
            Text(
                text = formatWidgetTime(item.end),
                maxLines = 1,
                style = GlanceTheme.widgetTypography().caption.copy(
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
            )
        }
        Spacer(GlanceModifier.width(spacing))
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .compatCornerBackground(container, WidgetShapes.LARGE)
                .clickable(action),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = GlanceModifier
                    .width(WidgetDimensions.subjectRailWidth)
                    .fillMaxHeight()
                    .padding(vertical = WidgetDimensions.spacingSmall)
                    .compatCornerBackground(
                        subjectColor ?: GlanceTheme.colors.outline,
                        WidgetShapes.FULL,
                    ),
            ) {}
            Row(
                modifier = GlanceModifier.fillMaxSize().padding(spacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = GlanceModifier.size(WidgetDimensions.iconSmall),
                    provider = ImageProvider(R.drawable.ic_widget_event),
                    contentDescription = eventTypeTitle(item.eventType),
                    colorFilter = ColorFilter.tint(subjectColor ?: GlanceTheme.colors.primary),
                )
                Spacer(GlanceModifier.width(spacing))
                Column(GlanceModifier.defaultWeight()) {
                    Text(
                        text = item.title ?: widgetString(R.string.widget_no_subject),
                        maxLines = 1,
                        style = GlanceTheme.widgetTypography().label.copy(color = titleColor),
                    )
                    Text(
                        text = eventTypeTitle(item.eventType),
                        maxLines = 1,
                        style = GlanceTheme.widgetTypography().caption.copy(
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
                if (!compactWidth && item.office != null) {
                    Spacer(GlanceModifier.width(spacing))
                    Image(
                        modifier = GlanceModifier.size(WidgetDimensions.iconExtraSmall),
                        provider = ImageProvider(R.drawable.ic_widget_room),
                        contentDescription = widgetString(R.string.widget_room_description),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    )
                    Spacer(GlanceModifier.width(WidgetDimensions.spacingExtraSmall))
                    Text(
                        text = item.office,
                        maxLines = 1,
                        style = GlanceTheme.widgetTypography().caption.copy(
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun eventTypeTitle(eventType: EventType): String = widgetString(
    when (eventType) {
        EventType.LESSON -> R.string.widget_event_lesson
        EventType.LECTURE -> R.string.widget_event_lecture
        EventType.PRACTICE -> R.string.widget_event_practice
        EventType.SEMINAR -> R.string.widget_event_seminar
        EventType.CLASS -> R.string.widget_event_class
        EventType.ONLINE_CLASS -> R.string.widget_event_online_class
        EventType.WEBINAR -> R.string.widget_event_webinar
    }
)
