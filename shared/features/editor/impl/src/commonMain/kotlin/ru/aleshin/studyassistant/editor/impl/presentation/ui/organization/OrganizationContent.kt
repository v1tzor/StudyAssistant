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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization

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
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.AvatarSection
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.EmailInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.HideButton
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationsInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.PhoneInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.WebInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store.OrganizationComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationNameInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationStatusChooser
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationTopBar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationTypeInfoField

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
@Composable
internal fun OrganizationContent(
    organizationComponent: OrganizationComponent,
    modifier: Modifier = Modifier,
) {
    val store = organizationComponent.store
    val state by store.stateAsState()
    val strings = EditorThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseOrganizationContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onUpdateAvatar = {
                    store.dispatchEvent(OrganizationEvent.UpdateAvatar(it))
                },
                onDeleteAvatar = {
                    store.dispatchEvent(OrganizationEvent.DeleteAvatar)
                },
                onSelectedType = {
                    store.dispatchEvent(OrganizationEvent.UpdateType(it))
                },
                onUpdateName = { short, full ->
                    store.dispatchEvent(OrganizationEvent.UpdateName(short, full))
                },
                onUpdateEmails = {
                    store.dispatchEvent(OrganizationEvent.UpdateEmails(it))
                },
                onUpdatePhones = {
                    store.dispatchEvent(OrganizationEvent.UpdatePhones(it))
                },
                onUpdateWebs = {
                    store.dispatchEvent(OrganizationEvent.UpdateWebs(it))
                },
                onUpdateLocations = {
                    store.dispatchEvent(OrganizationEvent.UpdateLocations(it))
                },
                onStatusChange = {
                    store.dispatchEvent(OrganizationEvent.UpdateStatus(it))
                },
                onHideOrganization = {
                    store.dispatchEvent(OrganizationEvent.HideOrganization)
                },
                onExceedingAvatarSizeLimit = {
                    coroutineScope.launch {
                        snackbarState.showSnackbar(
                            message = coreStrings.exceedingLimitImageSizeMessage,
                            withDismissAction = true,
                        )
                    }
                }
            )
        },
        topBar = {
            OrganizationTopBar(
                enabledSave = state.editableOrganization?.isValid() == true,
                onBackClick = { store.dispatchEvent(OrganizationEvent.NavigateToBack) },
                onSaveClick = { store.dispatchEvent(OrganizationEvent.SaveOrganization) },
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
            is OrganizationEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseOrganizationContent(
    state: OrganizationState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit,
    onSelectedType: (OrganizationType?) -> Unit,
    onUpdateName: (short: String?, full: String?) -> Unit,
    onUpdateEmails: (List<ContactInfoUi>) -> Unit,
    onUpdatePhones: (List<ContactInfoUi>) -> Unit,
    onUpdateWebs: (List<ContactInfoUi>) -> Unit,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onStatusChange: (Boolean) -> Unit,
    onHideOrganization: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val editableOrganization = state.editableOrganization
        AvatarSection(
            isLoading = state.isLoading,
            shortName = editableOrganization?.shortName,
            avatar = when (val actionWithAvatar = state.actionWithAvatar) {
                is ActionWithAvatar.None -> actionWithAvatar.uri
                is ActionWithAvatar.Set -> actionWithAvatar.file.uri
                is ActionWithAvatar.Delete -> null
            },
            onUpdateAvatar = onUpdateAvatar,
            onDeleteAvatar = onDeleteAvatar,
            onExceedingLimit = onExceedingAvatarSizeLimit,
        )
        OrganizationTypeInfoField(
            isLoading = state.isLoading,
            type = editableOrganization?.type,
            onSelected = onSelectedType,
        )
        OrganizationNameInfoField(
            isLoading = state.isLoading,
            shortName = editableOrganization?.shortName,
            fullName = editableOrganization?.fullName,
            onUpdateShortName = { onUpdateName(it, editableOrganization?.fullName) },
            onUpdateFullName = { onUpdateName(editableOrganization?.shortName, it) },
        )
        OrganizationStatusChooser(
            isLoading = state.isLoading,
            isMain = editableOrganization?.isMain ?: false,
            onStatusChange = onStatusChange,
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
                emails = editableOrganization?.emails ?: emptyList(),
                onUpdate = onUpdateEmails,
            )
            PhoneInfoFields(
                isLoading = state.isLoading,
                phones = editableOrganization?.phones ?: emptyList(),
                onUpdate = onUpdatePhones,
            )
            WebInfoFields(
                isLoading = state.isLoading,
                webs = editableOrganization?.webs ?: emptyList(),
                onUpdate = onUpdateWebs,
            )
            LocationsInfoFields(
                isLoading = state.isLoading,
                locations = editableOrganization?.locations ?: emptyList(),
                onUpdate = onUpdateLocations,
            )
        }

        if (editableOrganization?.uid?.isNotBlank() == true) {
            HideButton(
                onHide = onHideOrganization,
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                warningMessage = EditorThemeRes.strings.hideOrganizationWarning,
            )
        }
        Spacer(modifier = Modifier.height(56.dp))
    }
}