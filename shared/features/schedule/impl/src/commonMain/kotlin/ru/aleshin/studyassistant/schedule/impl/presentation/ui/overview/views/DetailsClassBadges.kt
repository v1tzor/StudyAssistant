/*
 * Copyright 2024 Stanislav Aleshin
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
import androidx.compose.ui.unit.dp
import entities.tasks.HomeworkStatus
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import theme.StudyAssistantRes
import views.InfoBadge

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
                    painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.green,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.greenContainer,
            content = { Text(text = ScheduleThemeRes.strings.homeworkIsCompleteShortTitle) }
        )
        HomeworkStatus.WAIT -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.orange,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.orangeContainer,
            content = { Text(text = ScheduleThemeRes.strings.homeworkInProgressShortTitle) }
        )
        HomeworkStatus.IN_FUTURE -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.yellow,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.yellowContainer,
            content = { Text(text = ScheduleThemeRes.strings.homeworkIsSetShortTitle) }
        )
        HomeworkStatus.NOT_COMPLETE -> InfoBadge(
            modifier = modifier,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.red,
                )
            },
            containerColor = StudyAssistantRes.colors.accents.redContainer,
            content = { Text(text = ScheduleThemeRes.strings.homeworkIsNotCompleteShortTitle) }
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
                painter = painterResource(StudyAssistantRes.icons.test),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        content = { Text(text = ScheduleThemeRes.strings.testLabel) }
    )
}