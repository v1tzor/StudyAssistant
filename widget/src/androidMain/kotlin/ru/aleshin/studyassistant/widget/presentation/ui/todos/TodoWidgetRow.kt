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
    val container = if (todo.status == WidgetTodoStatus.OVERDUE) {
        GlanceTheme.colors.errorContainer
    } else {
        GlanceTheme.colors.surfaceVariant
    }
    val deadlineColor = if (todo.status == WidgetTodoStatus.OVERDUE) {
        GlanceTheme.colors.error
    } else {
        GlanceTheme.colors.onSurfaceVariant
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(rowHeight)
            .compatCornerBackground(container, WidgetShapes.LARGE)
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
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
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
                    style = GlanceTheme.widgetTypography().label.copy(
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
                Spacer(GlanceModifier.width(WidgetDimensions.spacingExtraSmall))
                Box(
                    modifier = GlanceModifier
                        .size(WidgetDimensions.spacingSmall)
                        .compatCornerBackground(todo.priority.priorityColor(), WidgetShapes.FULL),
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
                    style = GlanceTheme.widgetTypography().caption.copy(
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TaskPriority.priorityColor(): ColorProvider = when (this) {
    TaskPriority.STANDARD -> GlanceTheme.colors.outline
    TaskPriority.MEDIUM -> GlanceTheme.colors.secondary
    TaskPriority.HIGH -> GlanceTheme.colors.error
}
