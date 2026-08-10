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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoBadge
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.homework_in_progress_short_title
import ru.aleshin.studyassistant.schedule.impl.resources.homework_is_complete_short_title
import ru.aleshin.studyassistant.schedule.impl.resources.homework_is_not_complete_short_title
import ru.aleshin.studyassistant.schedule.impl.resources.homework_is_set_short_title
import ru.aleshin.studyassistant.schedule.impl.resources.homework_is_skipped_short_title
import ru.aleshin.studyassistant.schedule.impl.resources.test_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_alert_circle as core_ic_alert_circle
import ru.aleshin.studyassistant.core.ui.resources.ic_tasks_outline as core_ic_tasks_outline

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
@Composable
internal fun DetailsClassHomeworkBadge(
    modifier: Modifier = Modifier,
    homeworkStatus: HomeworkStatus,
) {
    when (homeworkStatus) {
        HomeworkStatus.COMPLETE -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_tasks_outline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.green,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.greenContainer,
            content = {
                Text(
                    text = stringResource(Res.string.homework_is_complete_short_title),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        )

        HomeworkStatus.WAIT -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_tasks_outline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.orange,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.orangeContainer,
            content = {
                Text(
                    text = stringResource(Res.string.homework_in_progress_short_title),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        )

        HomeworkStatus.IN_FUTURE -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_tasks_outline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.yellow,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.yellowContainer,
            content = {
                Text(
                    text = stringResource(Res.string.homework_is_set_short_title),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        )

        HomeworkStatus.NOT_COMPLETE -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_tasks_outline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.red,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.redContainer,
            content = {
                Text(
                    text = stringResource(Res.string.homework_is_not_complete_short_title),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        )

        HomeworkStatus.SKIPPED -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_tasks_outline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            content = {
                Text(
                    text = stringResource(Res.string.homework_is_skipped_short_title),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        )
    }
}

@Composable
internal fun DetailsClassTestBadge(
    modifier: Modifier = Modifier,
) {
    InfoBadge(
        modifier = modifier,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(CoreRes.drawable.core_ic_alert_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        content = {
            Text(
                text = stringResource(Res.string.test_label),
                softWrap = false,
                overflow = TextOverflow.Visible,
                maxLines = 1,
            )
        }
    )
}