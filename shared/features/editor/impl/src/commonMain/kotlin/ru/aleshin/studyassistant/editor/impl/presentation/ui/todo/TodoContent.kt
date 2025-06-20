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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.TodoNotificationsUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TaskPriorityInfoView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoDeadlineInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoNotificationSelector

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Composable
internal fun TodoContent(
    state: TodoViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onTodoNameChange: (String) -> Unit,
    onTodoDescriptionChange: (String) -> Unit,
    onChangeDeadline: (Instant?) -> Unit,
    onChangePriority: (TaskPriority) -> Unit,
    onChangeNotifications: (TodoNotificationsUi) -> Unit,
    onOpenBillingScreen: () -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(top = 16.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TodoInfoFields(
            isLoading = isLoading,
            todoName = editableTodo?.name ?: "",
            todoDescription = editableTodo?.description,
            onTodoNameChange = onTodoNameChange,
            onTodoDescriptionChange = onTodoDescriptionChange,
        )
        TodoDeadlineInfoFields(
            isLoading = isLoading,
            deadline = editableTodo?.deadline,
            onChangeDeadline = onChangeDeadline,
        )
        TaskPriorityInfoView(
            isLoading = isLoading,
            priority = editableTodo?.priority,
            onChangePriority = onChangePriority,
        )
        TodoNotificationSelector(
            isPaidUser = isPaidUser,
            notifications = editableTodo?.notifications,
            onChangeNotifications = onChangeNotifications,
            onOpenBillingScreen = onOpenBillingScreen,
        )
    }
}