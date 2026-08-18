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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.info.impl.presentation.ui.common.EmployeeSubjectView
import ru.aleshin.studyassistant.info.impl.presentation.ui.common.NoneEmployeeSubjectView
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.delete_employee_warning_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_email as core_ic_email
import ru.aleshin.studyassistant.core.ui.resources.ic_phone as core_ic_phone
import ru.aleshin.studyassistant.core.ui.resources.ic_web as core_ic_web
import ru.aleshin.studyassistant.core.ui.resources.warning_delete_confirm_title as core_warning_delete_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.warning_dialog_title as core_warning_dialog_title

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailsEmployeeViewItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useExpandedStyle: Boolean = false,
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
    var deleteWarningDialogStatus by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissBoxValue ->
            when (dismissBoxValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onEdit()
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    deleteWarningDialogStatus = true
                }

                SwipeToDismissBoxValue.Settled -> Unit
            }
            return@rememberSwipeToDismissBoxState false
        },
        positionalThreshold = { it * .50f },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                startToEndContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
                endToStartContent = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                    )
                },
                startToEndColor = MaterialTheme.colorScheme.errorContainer,
                endToStartColor = StudyAssistantRes.colors.accents.orangeContainer,
            )
        },
        enableDismissFromEndToStart = enabled,
        enableDismissFromStartToEnd = enabled,
    ) {
        DetailsEmployeeView(
            onClick = onOpenProfile,
            useExpandedStyle = useExpandedStyle,
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

    if (deleteWarningDialogStatus) {
        WarningAlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = stringResource(CoreRes.string.core_warning_dialog_title)) },
            text = { Text(text = stringResource(Res.string.delete_employee_warning_title)) },
            confirmTitle = stringResource(CoreRes.string.core_warning_delete_confirm_title),
            onDismiss = { deleteWarningDialogStatus = false },
            onConfirm = {
                onDelete()
                deleteWarningDialogStatus = false
            },
        )
    }
}

@Composable
private fun DetailsEmployeeView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useExpandedStyle: Boolean = false,
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
    val shape = if (useExpandedStyle) {
        MaterialTheme.shapes.extraLarge
    } else {
        MaterialTheme.shapes.large
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(if (useExpandedStyle) 16.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AvatarView(
                modifier = modifier.size(40.dp),
                firstName = firstName,
                secondName = patronymic ?: secondName,
                imageUrl = avatar,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailsEmployeeViewContent(
                    modifier = Modifier.weight(1f),
                    useExpandedStyle = useExpandedStyle,
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
    useExpandedStyle: Boolean = false,
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
                text = post.mapToString(),
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
                style = if (useExpandedStyle) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.titleMedium
                },
            )
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (subjects.isNotEmpty()) {
                items(subjects, key = { subject -> subject.uid }) { subject ->
                    EmployeeSubjectView(
                        color = Color(subject.color),
                        text = subject.name,
                    )
                }
            } else {
                item(key = EMPTY_EMPLOYEE_SUBJECTS_KEY) {
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
                painter = painterResource(CoreRes.drawable.core_ic_phone),
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.green,
            )
        }
        if (isHaveEmail) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(CoreRes.drawable.core_ic_email),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (isHaveWebsite) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(CoreRes.drawable.core_ic_web),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

private const val EMPTY_EMPLOYEE_SUBJECTS_KEY = "empty_employee_subjects"