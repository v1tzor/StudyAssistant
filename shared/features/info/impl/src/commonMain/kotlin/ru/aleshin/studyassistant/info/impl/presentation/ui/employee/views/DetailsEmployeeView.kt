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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.common.EmployeeAvatarView
import ru.aleshin.studyassistant.info.impl.presentation.ui.common.EmployeeSubjectView
import ru.aleshin.studyassistant.info.impl.presentation.ui.common.NoneEmployeeSubjectView

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailsEmployeeViewItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    avatar: String?,
    post: EmployeePost,
    firstName: String,
    secondName: String?,
    patronymic: String?,
    subjects: List<SubjectUi>,
    isHavePhone: Boolean,
    isHaveEmail: Boolean,
    isHaveWebsite: Boolean,
    onOpenProfile: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissBoxValue ->
            when (dismissBoxValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * .50f },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                endToStartContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
                startToEndContent = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                    )
                },
                endToStartColor = MaterialTheme.colorScheme.errorContainer,
                startToEndColor = StudyAssistantRes.colors.accents.orangeContainer,
            )
        },
        enableDismissFromEndToStart = enabled,
        enableDismissFromStartToEnd = enabled,
    ) {
        DetailsEmployeeView(
            onClick = onOpenProfile,
            avatar = avatar,
            post = post,
            firstName = firstName,
            secondName = secondName,
            patronymic = patronymic,
            subjects = subjects,
            isHavePhone = isHavePhone,
            isHaveEmail = isHaveEmail,
            isHaveWebsite = isHaveWebsite,
        )
    }
}

@Composable
private fun DetailsEmployeeView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    avatar: String?,
    post: EmployeePost,
    firstName: String,
    secondName: String?,
    patronymic: String?,
    subjects: List<SubjectUi>,
    isHavePhone: Boolean,
    isHaveEmail: Boolean,
    isHaveWebsite: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EmployeeAvatarView(
                firstName = firstName,
                secondName = secondName ?: patronymic,
                imageUrl = avatar,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailsEmployeeViewContent(
                    modifier = Modifier.weight(1f),
                    post = post,
                    firstName = firstName,
                    secondName = secondName,
                    patronymic = patronymic,
                    subjects = subjects,
                )
                DetailsEmployeeViewTrailing(
                    isHavePhone = isHavePhone,
                    isHaveEmail = isHaveEmail,
                    isHaveWebsite = isHaveWebsite,
                )
            }
        }
    }
}

@Composable
private fun DetailsEmployeeViewContent(
    modifier: Modifier = Modifier,
    post: EmployeePost,
    firstName: String,
    secondName: String?,
    patronymic: String?,
    subjects: List<SubjectUi>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column {
            Text(
                text = post.mapToString(StudyAssistantRes.strings),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = buildString {
                    append(firstName)
                    if (patronymic != null) append(" ", patronymic)
                    if (secondName != null) append(" ", secondName)
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (subjects.isNotEmpty()) {
                items(subjects) { subject ->
                    EmployeeSubjectView(
                        color = Color(subject.color),
                        text = subject.name,
                    )
                }
            } else {
                item {
                    NoneEmployeeSubjectView()
                }
            }
        }
    }
}

@Composable
private fun DetailsEmployeeViewTrailing(
    modifier: Modifier = Modifier,
    isHavePhone: Boolean,
    isHaveEmail: Boolean,
    isHaveWebsite: Boolean,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isHavePhone) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(StudyAssistantRes.icons.phone),
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.green,
            )
        }
        if (isHaveEmail) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(StudyAssistantRes.icons.email),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (isHaveWebsite) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(StudyAssistantRes.icons.website),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}