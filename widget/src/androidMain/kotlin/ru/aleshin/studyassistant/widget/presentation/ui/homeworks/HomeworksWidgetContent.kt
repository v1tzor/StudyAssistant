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
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.text.Text
import ru.aleshin.studyassistant.core.common.navigation.WidgetDeepLinkDestination
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.presentation.actions.RefreshWidgetsAction
import ru.aleshin.studyassistant.widget.presentation.models.HomeworksWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.navigation.WidgetDeepLinkFactory
import ru.aleshin.studyassistant.widget.presentation.theme.formatWidgetDate
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetDimensions
import ru.aleshin.studyassistant.widget.presentation.theme.widgetString
import ru.aleshin.studyassistant.widget.presentation.theme.widgetTypography
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetHeader
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetScaffold
import ru.aleshin.studyassistant.widget.presentation.ui.common.WidgetStateContent
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
@Composable
fun HomeworksWidgetContent(state: HomeworksWidgetStateUi) {
    val context = LocalContext.current
    val sizeClass = WidgetSizeClass.fetch(LocalSize.current)
    val listDestination = WidgetDeepLinkDestination.Homeworks

    WidgetScaffold(
        header = {
            WidgetHeader(
                title = widgetString(R.string.widget_homeworks_title),
                titleAction = actionStartActivity(
                    WidgetDeepLinkFactory.createIntent(context, listDestination),
                ),
                actionIcon = ImageProvider(R.drawable.ic_widget_add),
                actionDescription = widgetString(R.string.widget_add_homework_description),
                action = actionStartActivity(
                    WidgetDeepLinkFactory.createIntent(
                        context = context,
                        destination = WidgetDeepLinkDestination.HomeworkEditor(
                            homeworkId = null,
                            date = System.currentTimeMillis(),
                            subjectId = null,
                            organizationId = null,
                        ),
                    ),
                ),
                secondaryActionIcon = ImageProvider(R.drawable.ic_widget_refresh),
                secondaryActionDescription = widgetString(R.string.widget_refresh_description),
                secondaryAction = actionRunCallback<RefreshWidgetsAction>(),
            )
        },
    ) {
        WidgetStateContent(
            status = state.status,
            isStale = state.isStale,
            emptyTitle = widgetString(R.string.widget_homeworks_empty),
        ) {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                state.groups.forEach { group ->
                    item(itemId = group.id) {
                        Text(
                            modifier = GlanceModifier.padding(
                                top = WidgetDimensions.spacingExtraSmall,
                                bottom = WidgetDimensions.spacingExtraSmall,
                            ),
                            text = formatWidgetDate(group.date),
                            maxLines = 1,
                            style = GlanceTheme.widgetTypography().caption.copy(
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                        )
                    }
                    items(group.items, itemId = { it.id }) { homework ->
                        Box(GlanceModifier.padding(bottom = WidgetDimensions.spacingSmall)) {
                            HomeworkWidgetRow(
                                homework = homework,
                                sizeClass = sizeClass,
                                action = actionStartActivity(
                                    WidgetDeepLinkFactory.createIntent(
                                        context = context,
                                        destination = WidgetDeepLinkDestination.HomeworkEditor(
                                            homeworkId = homework.homeworkId,
                                            date = homework.deadline,
                                            subjectId = homework.subjectId,
                                            organizationId = homework.organizationId,
                                        ),
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
