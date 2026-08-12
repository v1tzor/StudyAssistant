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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiToolConfirmationUi
import ru.aleshin.studyassistant.chat.impl.resources.Res
import ru.aleshin.studyassistant.chat.impl.resources.class_field_label
import ru.aleshin.studyassistant.chat.impl.resources.completed_field_label
import ru.aleshin.studyassistant.chat.impl.resources.confirm_change_button
import ru.aleshin.studyassistant.chat.impl.resources.confirm_change_description
import ru.aleshin.studyassistant.chat.impl.resources.confirm_class_creation_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_class_deletion_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_class_update_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_generic_change_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_homework_completion_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_homework_creation_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_homework_deletion_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_homework_update_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_todo_completion_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_todo_creation_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_todo_deletion_title
import ru.aleshin.studyassistant.chat.impl.resources.confirm_todo_update_title
import ru.aleshin.studyassistant.chat.impl.resources.custom_data_field_label
import ru.aleshin.studyassistant.chat.impl.resources.date_field_label
import ru.aleshin.studyassistant.chat.impl.resources.deadline_field_label
import ru.aleshin.studyassistant.chat.impl.resources.description_field_label
import ru.aleshin.studyassistant.chat.impl.resources.employee_field_label
import ru.aleshin.studyassistant.chat.impl.resources.end_time_field_label
import ru.aleshin.studyassistant.chat.impl.resources.event_type_field_label
import ru.aleshin.studyassistant.chat.impl.resources.location_field_label
import ru.aleshin.studyassistant.chat.impl.resources.name_field_label
import ru.aleshin.studyassistant.chat.impl.resources.office_field_label
import ru.aleshin.studyassistant.chat.impl.resources.organization_field_label
import ru.aleshin.studyassistant.chat.impl.resources.practical_tasks_field_label
import ru.aleshin.studyassistant.chat.impl.resources.presentation_tasks_field_label
import ru.aleshin.studyassistant.chat.impl.resources.priority_field_label
import ru.aleshin.studyassistant.chat.impl.resources.reject_change_button
import ru.aleshin.studyassistant.chat.impl.resources.start_time_field_label
import ru.aleshin.studyassistant.chat.impl.resources.subject_field_label
import ru.aleshin.studyassistant.chat.impl.resources.target_field_label
import ru.aleshin.studyassistant.chat.impl.resources.test_topic_field_label
import ru.aleshin.studyassistant.chat.impl.resources.theoretical_tasks_field_label

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Composable
internal fun AiToolConfirmationCard(
    confirmation: AiToolConfirmationUi,
    enabled: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = confirmationTitle(confirmation.name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(Res.string.confirm_change_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            confirmation.visibleArguments().forEach { (label, value) ->
                Text(
                    text = "${argumentLabel(label)}: $value",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = onReject,
                ) {
                    Text(text = stringResource(Res.string.reject_change_button))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = onConfirm,
                ) {
                    Text(text = stringResource(Res.string.confirm_change_button))
                }
            }
        }
    }
}

private fun AiToolConfirmationUi.visibleArguments(): List<Pair<String, String>> {
    return arguments.mapNotNull { (name, value) ->
        if (name.endsWith("Id") || value.isBlank()) null else name to value
    }
}

@Composable
private fun confirmationTitle(name: String): String = stringResource(
    when (name) {
        "create_todo" -> Res.string.confirm_todo_creation_title
        "update_todo" -> Res.string.confirm_todo_update_title
        "complete_todo" -> Res.string.confirm_todo_completion_title
        "delete_todo" -> Res.string.confirm_todo_deletion_title
        "create_homework" -> Res.string.confirm_homework_creation_title
        "update_homework" -> Res.string.confirm_homework_update_title
        "complete_homework" -> Res.string.confirm_homework_completion_title
        "delete_homework" -> Res.string.confirm_homework_deletion_title
        "create_class" -> Res.string.confirm_class_creation_title
        "update_class" -> Res.string.confirm_class_update_title
        "delete_class" -> Res.string.confirm_class_deletion_title
        else -> Res.string.confirm_generic_change_title
    },
)

@Composable
private fun argumentLabel(name: String): String = stringResource(
    when (name) {
        "name" -> Res.string.name_field_label
        "description" -> Res.string.description_field_label
        "deadline" -> Res.string.deadline_field_label
        "priority" -> Res.string.priority_field_label
        "theoreticalTasks" -> Res.string.theoretical_tasks_field_label
        "practicalTasks" -> Res.string.practical_tasks_field_label
        "presentationTasks" -> Res.string.presentation_tasks_field_label
        "testTopic" -> Res.string.test_topic_field_label
        "target" -> Res.string.target_field_label
        "completed" -> Res.string.completed_field_label
        "classId" -> Res.string.class_field_label
        "organizationId" -> Res.string.organization_field_label
        "organization" -> Res.string.organization_field_label
        "subjectId" -> Res.string.subject_field_label
        "subject" -> Res.string.subject_field_label
        "date" -> Res.string.date_field_label
        "startTime" -> Res.string.start_time_field_label
        "endTime" -> Res.string.end_time_field_label
        "eventType" -> Res.string.event_type_field_label
        "customData" -> Res.string.custom_data_field_label
        "employeeId" -> Res.string.employee_field_label
        "employee" -> Res.string.employee_field_label
        "office" -> Res.string.office_field_label
        "location" -> Res.string.location_field_label
        else -> Res.string.description_field_label
    },
)
