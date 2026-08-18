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

package ru.aleshin.studyassistant.widget.presentation.ui.todos

import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
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
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodoStatus
import ru.aleshin.studyassistant.widget.presentation.actions.CompleteTodoAction
import ru.aleshin.studyassistant.widget.presentation.models.TodoWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.theme.compatCornerBackground
import ru.aleshin.studyassistant.widget.presentation.theme.formatWidgetDate
import ru.aleshin.studyassistant.widget.presentation.theme.formatWidgetTime
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
fun TodoWidgetRow(
    todo: TodoWidgetItemUi,
    sizeClass: WidgetSizeClass,
    action: Action,
) {
    val compact = sizeClass.width == WidgetSizeClass.Width.COMPACT ||
        sizeClass.height == WidgetSizeClass.Height.COMPACT
    val expanded = sizeClass.width == WidgetSizeClass.Width.EXPANDED
    val rowHeight = if (expanded && !compact) {
        WidgetDimensions.todoRowHeightExpanded
    } else {
        WidgetDimensions.todoRowHeight
    }
    val palette = todo.colorPalette()
    val deadlineColor = if (todo.status == WidgetTodoStatus.OVERDUE) {
        palette.accent
    } else {
        palette.onContainer
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(rowHeight)
            .compatCornerBackground(palette.container, WidgetShapes.LARGE)
            .clickable(action)
            .padding(horizontal = WidgetDimensions.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(WidgetDimensions.touchTarget)
                .clickable(
                    actionRunCallback<CompleteTodoAction>(
                        actionParametersOf(CompleteTodoAction.TodoIdKey to todo.todoId),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = GlanceModifier.size(WidgetDimensions.checkboxSize),
                provider = ImageProvider(R.drawable.ic_widget_checkbox),
                contentDescription = widgetString(R.string.widget_complete_todo_description),
                colorFilter = ColorFilter.tint(palette.accent),
            )
        }
        Column(GlanceModifier.defaultWeight()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = GlanceModifier.defaultWeight(),
                    text = todo.name,
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().label.copy(color = palette.onContainer),
                )
                Spacer(GlanceModifier.width(WidgetDimensions.spacingExtraSmall))
                Box(
                    modifier = GlanceModifier
                        .size(WidgetDimensions.priorityDotSize)
                        .compatCornerBackground(palette.accent, WidgetShapes.FULL),
                ) {}
            }
            todo.deadline?.let { deadline ->
                Text(
                    text = "${formatWidgetDate(deadline)} · ${formatWidgetTime(deadline)}",
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().caption.copy(color = deadlineColor),
                )
            }
            if (expanded && !compact && todo.description != null) {
                Text(
                    text = todo.description,
                    maxLines = 1,
                    style = GlanceTheme.widgetTypography().caption.copy(color = palette.onContainer),
                )
            }
        }
    }
}

@Composable
private fun TodoWidgetItemUi.colorPalette(): TodoColorPalette {
    val accents = widgetAccents()
    return when {
        status == WidgetTodoStatus.OVERDUE -> TodoColorPalette(
            accent = accents.red,
            container = accents.redContainer,
            onContainer = accents.onRedContainer,
        )
        priority == TaskPriority.HIGH -> TodoColorPalette(
            accent = accents.red,
            container = accents.redContainer,
            onContainer = accents.onRedContainer,
        )
        priority == TaskPriority.MEDIUM -> TodoColorPalette(
            accent = accents.orange,
            container = accents.orangeContainer,
            onContainer = accents.onOrangeContainer,
        )
        else -> TodoColorPalette(
            accent = GlanceTheme.colors.primary,
            container = GlanceTheme.colors.primaryContainer,
            onContainer = GlanceTheme.colors.onPrimaryContainer,
        )
    }
}

private data class TodoColorPalette(
    val accent: ColorProvider,
    val container: ColorProvider,
    val onContainer: ColorProvider,
)
