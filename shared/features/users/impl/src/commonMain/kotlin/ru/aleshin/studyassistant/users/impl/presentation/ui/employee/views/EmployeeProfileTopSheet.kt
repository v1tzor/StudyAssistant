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

package ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.bottomSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.core.ui.views.shortTimeFormat
import ru.aleshin.studyassistant.users.impl.presentation.models.ContactInfoUi
import ru.aleshin.studyassistant.users.impl.presentation.models.EmployeeDetailsUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
@Composable
internal fun EmployeeTopSheet(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    employee: EmployeeDetailsUi?,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (!loading) {
                    EmployeeTopSheetHeader(
                        firstName = employee?.firstName ?: "",
                        secondName = employee?.secondName,
                        patronymic = employee?.patronymic,
                        avatar = employee?.avatar,
                        email = employee?.emails?.getOrNull(0),
                        phone = employee?.phones?.getOrNull(0),
                    )
                    EmployeeTopSheetFooter(
                        post = employee?.post ?: EmployeePost.EMPLOYEE,
                        birthday = employee?.birthday,
                        workTimeStart = employee?.workTimeStart,
                        workTimeEnd = employee?.workTimeEnd,
                    )
                } else {
                    EmployeeTopSheetHeaderPlaceholder()
                    EmployeeTopSheetFooterPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun EmployeeTopSheetHeaderPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.full,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
        Column {
            PlaceholderBox(
                modifier = Modifier.height(25.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            PlaceholderBox(
                modifier = Modifier.height(20.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
    }
}

@Composable
private fun EmployeeTopSheetFooterPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.size(105.dp, 68.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        )
        PlaceholderBox(
            modifier = Modifier.size(105.dp, 68.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        )
        PlaceholderBox(
            modifier = Modifier.size(105.dp, 68.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun EmployeeTopSheetHeader(
    modifier: Modifier = Modifier,
    firstName: String,
    secondName: String?,
    patronymic: String?,
    avatar: String?,
    email: ContactInfoUi?,
    phone: ContactInfoUi?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AvatarView(
            modifier = modifier.size(120.dp),
            firstName = firstName,
            secondName = patronymic ?: secondName,
            imageUrl = avatar,
            style = MaterialTheme.typography.headlineLarge,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxLines = 2,
            ) {
                Text(
                    text = firstName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (patronymic != null) {
                    Text(
                        text = patronymic,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (secondName != null) {
                    Text(
                        text = secondName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            if (email != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = email.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else if (phone != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = phone.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeeTopSheetFooter(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    post: EmployeePost,
    birthday: String?,
    workTimeStart: Instant?,
    workTimeEnd: Instant?,
) {
    LazyRow(
        modifier = modifier,
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        item {
            EmployeeInfoView(
                icon = Icons.Outlined.PersonOutline,
                label = UsersThemeRes.strings.employeePostLabel,
                title = post.mapToString(StudyAssistantRes.strings),
            )
        }
        if (workTimeStart != null || workTimeEnd != null) {
            item { VerticalDivider(modifier = Modifier.height(56.dp)) }
            item {
                EmployeeInfoView(
                    icon = Icons.Outlined.WorkOutline,
                    label = UsersThemeRes.strings.employeeWorkTimeLabel,
                    title = buildString {
                        if (workTimeStart != null) {
                            append(workTimeStart.formatByTimeZone(DateTimeComponents.Formats.shortTimeFormat()))
                        } else {
                            append("*")
                        }
                        if (workTimeEnd != null) {
                            append(" ", "-", " ")
                            append(workTimeEnd.formatByTimeZone(DateTimeComponents.Formats.shortTimeFormat()))
                        }
                    },
                )
            }
        }
        if (birthday != null) {
            item { VerticalDivider(modifier = Modifier.height(56.dp)) }
            item {
                EmployeeInfoView(
                    icon = Icons.Outlined.Cake,
                    label = UsersThemeRes.strings.employeeBirthdayLabel,
                    title = birthday,
                )
            }
        }
    }
}

@Composable
private fun EmployeeInfoView(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    title: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}