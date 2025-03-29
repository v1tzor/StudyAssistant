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

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworksStatus
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 26.03.2025.
 */
@Composable
fun HomeworksCompleteBadge(
    modifier: Modifier = Modifier,
    listStatus: DailyHomeworksStatus,
    totalHomeworks: Int,
    completedHomeworks: Int,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = when (listStatus) {
            DailyHomeworksStatus.COMPLETE_ALL -> StudyAssistantRes.colors.accents.greenContainer
            DailyHomeworksStatus.IN_COMING -> StudyAssistantRes.colors.accents.orangeContainer
            DailyHomeworksStatus.IN_FUTURE -> MaterialTheme.colorScheme.primaryContainer
            DailyHomeworksStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
            DailyHomeworksStatus.EMPTY -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when (listStatus) {
            DailyHomeworksStatus.COMPLETE_ALL -> StudyAssistantRes.colors.accents.onGreenContainer
            DailyHomeworksStatus.IN_COMING -> StudyAssistantRes.colors.accents.onOrangeContainer
            DailyHomeworksStatus.IN_FUTURE -> MaterialTheme.colorScheme.onPrimaryContainer
            DailyHomeworksStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            DailyHomeworksStatus.EMPTY -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            text = buildString {
                append(completedHomeworks)
                append('/')
                append(totalHomeworks)
            },
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}