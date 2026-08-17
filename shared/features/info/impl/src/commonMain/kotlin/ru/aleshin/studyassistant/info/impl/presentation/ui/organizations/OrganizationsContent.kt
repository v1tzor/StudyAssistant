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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.InfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.fetchInfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationsBottomBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationsExpandedTopBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationsTopBar
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.copy_message

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationsContent(
    organizationsComponent: OrganizationsComponent,
    modifier: Modifier = Modifier
) {
    val store = organizationsComponent.store
    val state by store.stateAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val copyMessage = stringResource(Res.string.copy_message)
    val pagerState = rememberPagerState { (state.shortOrganizations?.size ?: 0) + 1 }
    val pagerOrganizationId by derivedStateOf { state.shortOrganizations?.getOrNull(pagerState.currentPage)?.uid }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchInfoLayoutMode()
    val organizationId = if (layoutMode == InfoLayoutMode.EXPANDED) {
        state.organizationData?.uid ?: state.shortOrganizations?.firstOrNull()?.uid
    } else {
        pagerOrganizationId
    }

    LaunchedEffect(layoutMode, state.shortOrganizations, state.organizationData?.uid) {
        if (layoutMode == InfoLayoutMode.EXPANDED && state.organizationData == null) {
            val firstOrganizationId = state.shortOrganizations?.firstOrNull()?.uid
            if (firstOrganizationId != null) {
                store.dispatchEvent(OrganizationsEvent.ChangeOrganization(firstOrganizationId))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (layoutMode == InfoLayoutMode.EXPANDED) {
                OrganizationsExpandedTopBar()
            } else {
                OrganizationsTopBar()
            }
        },
        bottomBar = {
            if (layoutMode == InfoLayoutMode.COMPACT) {
                OrganizationsBottomBar(
                    pagerState = pagerState,
                    allOrganizations = state.shortOrganizations,
                    organizationData = state.organizationData,
                    onChangeOrganization = {
                        store.dispatchEvent(OrganizationsEvent.ChangeOrganization(it?.uid))
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets()
    ) { paddingValues ->
        OrganizationsLayout(
            modifier = Modifier.padding(paddingValues),
            layoutMode = layoutMode,
            state = state,
            onRefresh = {
                store.dispatchEvent(OrganizationsEvent.Refresh(organizationId))
            },
            onAddOrganization = {
                store.dispatchEvent(OrganizationsEvent.ClickEditOrganization(null))
            },
            onEditOrganization = {
                store.dispatchEvent(OrganizationsEvent.ClickEditOrganization(organizationId))
            },
            onSelectOrganization = {
                store.dispatchEvent(OrganizationsEvent.ChangeOrganization(it))
            },
            onEditOrganizationId = { organization ->
                store.dispatchEvent(OrganizationsEvent.ClickEditOrganization(organization))
            },
            onCopyContactInfo = {
                clipboardManager.setText(AnnotatedString(it.value))
                coroutineScope.launch { snackbarState.showSnackbar(copyMessage) }
            },
            onShowAllEmployee = {
                store.dispatchEvent(OrganizationsEvent.ClickShowAllEmployees(checkNotNull(organizationId)))
            },
            onShowEmployeeProfile = {
                store.dispatchEvent(OrganizationsEvent.ClickEmployee(it))
            },
            onShowAllSubjects = {
                store.dispatchEvent(OrganizationsEvent.ClickShowAllSubjects(checkNotNull(organizationId)))
            },
            onShowSubjectEditor = {
                store.dispatchEvent(OrganizationsEvent.ClickEditSubject(it, checkNotNull(organizationId)))
            }
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is OrganizationsEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
