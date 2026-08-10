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
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import ru.aleshin.studyassistant.core.common.navigation.WidgetDeepLinkDestination
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.presentation.actions.RefreshWidgetsAction
import ru.aleshin.studyassistant.widget.presentation.models.GoalsWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.navigation.WidgetDeepLinkFactory
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetHeader
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetScaffold
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetStateContent
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
@Composable
fun GoalsWidgetContent(state: GoalsWidgetStateUi) {
    val context = LocalContext.current
    val sizeClass = WidgetSizeClass.fetch(LocalSize.current)
    val goalsDestination = WidgetDeepLinkDestination.Goals
    val goalsAction = actionStartActivity(
        WidgetDeepLinkFactory.createIntent(context, goalsDestination),
    )

    WidgetScaffold(
        header = {
            WidgetHeader(
                title = widgetString(R.string.widget_goals_title),
                titleAction = goalsAction,
                actionIcon = ImageProvider(R.drawable.ic_widget_add),
                actionDescription = widgetString(R.string.widget_add_goal_description),
                action = goalsAction,
                secondaryActionIcon = ImageProvider(R.drawable.ic_widget_refresh),
                secondaryActionDescription = widgetString(R.string.widget_refresh_description),
                secondaryAction = actionRunCallback<RefreshWidgetsAction>(),
            )
        },
    ) {
        WidgetStateContent(
            status = state.status,
            isStale = state.isStale,
            emptyTitle = widgetString(R.string.widget_goals_empty),
        ) {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                items(state.items, itemId = { it.id }) { goal ->
                    Box(GlanceModifier.padding(bottom = WidgetDimensions.spacingSmall)) {
                        GoalWidgetRow(
                            goal = goal,
                            sizeClass = sizeClass,
                            action = goalsAction,
                        )
                    }
                }
            }
        }
    }
}
