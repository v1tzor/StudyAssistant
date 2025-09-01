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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.BirthdayInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.EmailInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationsInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.PhoneInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.WebInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store.EmployeeComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.EmployeeAvatarSection
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.EmployeeNameInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.EmployeePostInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.EmployeeTopBar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.WorkTimeInfoField

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
@Composable
internal fun EmployeeContent(
    employeeComponent: EmployeeComponent,
    modifier: Modifier = Modifier,
) {
    val store = employeeComponent.store
    val state by store.stateAsState()
    val strings = EditorThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseEmployeeContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onUpdateAvatar = { store.dispatchEvent(EmployeeEvent.UpdateAvatar(it)) },
                onDeleteAvatar = { store.dispatchEvent(EmployeeEvent.DeleteAvatar) },
                onEmployeePostSelected = { store.dispatchEvent(EmployeeEvent.UpdatePost(it)) },
                omBirthdaySelected = { store.dispatchEvent(EmployeeEvent.UpdateBirthday(it)) },
                onUpdateEmails = { store.dispatchEvent(EmployeeEvent.UpdateEmails(it)) },
                onUpdatePhones = { store.dispatchEvent(EmployeeEvent.UpdatePhones(it)) },
                onUpdateWebs = { store.dispatchEvent(EmployeeEvent.UpdateWebs(it)) },
                onUpdateLocations = { store.dispatchEvent(EmployeeEvent.UpdateLocations(it)) },
                omWorkTimeSelected = { start, end ->
                    store.dispatchEvent(EmployeeEvent.UpdateWorkTime(start, end))
                },
                onUpdateName = { first, second, patronymic ->
                    store.dispatchEvent(EmployeeEvent.UpdateName(first, second, patronymic))
                },
                onExceedingAvatarSizeLimit = {
                    coroutineScope.launch {
                        snackbarState.showSnackbar(
                            message = coreStrings.exceedingLimitImageSizeMessage,
                            withDismissAction = true,
                        )
                    }
                },
            )
        },
        topBar = {
            EmployeeTopBar(
                enabledSave = state.editableEmployee?.isValid() ?: false,
                onBackClick = { store.dispatchEvent(EmployeeEvent.NavigateToBack) },
                onSaveClick = { store.dispatchEvent(EmployeeEvent.SaveEmployee) },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is EmployeeEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseEmployeeContent(
    state: EmployeeState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit,
    onUpdateName: (first: String?, second: String?, patronymic: String?) -> Unit,
    onEmployeePostSelected: (EmployeePost?) -> Unit,
    omWorkTimeSelected: (Instant?, Instant?) -> Unit,
    omBirthdaySelected: (String?) -> Unit,
    onUpdateEmails: (List<ContactInfoUi>) -> Unit,
    onUpdatePhones: (List<ContactInfoUi>) -> Unit,
    onUpdateWebs: (List<ContactInfoUi>) -> Unit,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val editableEmployee = state.editableEmployee
        EmployeeAvatarSection(
            isLoading = state.isLoading,
            firstName = editableEmployee?.firstName,
            lastName = editableEmployee?.patronymic ?: editableEmployee?.secondName,
            avatar = when (state.actionWithAvatar) {
                is ActionWithAvatar.None -> state.actionWithAvatar.uri
                is ActionWithAvatar.Set -> state.actionWithAvatar.file.uri
                is ActionWithAvatar.Delete -> null
            },
            onUpdateAvatar = onUpdateAvatar,
            onDeleteAvatar = onDeleteAvatar,
            onExceedingAvatarSizeLimit = onExceedingAvatarSizeLimit,
        )
        EmployeeNameInfoField(
            isLoading = state.isLoading,
            firstName = editableEmployee?.firstName,
            secondName = editableEmployee?.secondName,
            patronymic = editableEmployee?.patronymic,
            onUpdateFirstName = { onUpdateName(it, editableEmployee?.secondName, editableEmployee?.patronymic) },
            onUpdateSecondName = { onUpdateName(editableEmployee?.firstName, it, editableEmployee?.patronymic) },
            onUpdatePatronymic = { onUpdateName(editableEmployee?.firstName, editableEmployee?.secondName, it) },
        )
        EmployeePostInfoField(
            isLoading = state.isLoading,
            post = editableEmployee?.post,
            onSelected = onEmployeePostSelected,
        )
        WorkTimeInfoField(
            isLoading = state.isLoading,
            startTime = editableEmployee?.workTimeStart,
            endTime = editableEmployee?.workTimeEnd,
            onSelected = omWorkTimeSelected,
        )
        BirthdayInfoField(
            isLoading = state.isLoading,
            birthday = editableEmployee?.birthday,
            onSelected = omBirthdaySelected,
        )
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = EditorThemeRes.strings.contactInfoSectionHeader,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                )
                HorizontalDivider()
            }
            EmailInfoFields(
                isLoading = state.isLoading,
                emails = editableEmployee?.emails ?: emptyList(),
                onUpdate = onUpdateEmails,
            )
            PhoneInfoFields(
                isLoading = state.isLoading,
                phones = editableEmployee?.phones ?: emptyList(),
                onUpdate = onUpdatePhones,
            )
            WebInfoFields(
                isLoading = state.isLoading,
                webs = editableEmployee?.webs ?: emptyList(),
                onUpdate = onUpdateWebs,
            )
            LocationsInfoFields(
                isLoading = state.isLoading,
                locations = editableEmployee?.locations ?: emptyList(),
                onUpdate = onUpdateLocations,
            )
        }
        Spacer(modifier = Modifier.height(56.dp))
    }
}