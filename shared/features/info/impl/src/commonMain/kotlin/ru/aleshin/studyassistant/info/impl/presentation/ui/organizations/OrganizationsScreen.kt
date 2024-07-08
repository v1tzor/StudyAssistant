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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsViewState
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.screenmodel.rememberOrganizationsScreenModel
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationsBottomBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationsTopBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
internal class OrganizationsScreen : Screen {

    @Composable
    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun Content() = ScreenContent(
        screenModel = rememberOrganizationsScreenModel(),
        initialState = OrganizationsViewState(),
    ) { state ->
        val strings = InfoThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val clipboardManager = LocalClipboardManager.current
        val snackbarState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState { (state.shortOrganizations?.size ?: 0) + 1 }
        val refreshState = rememberPullToRefreshState()
        val organizationId by derivedStateOf { state.shortOrganizations?.getOrNull(pagerState.currentPage)?.uid }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                OrganizationsContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    refreshState = refreshState,
                    onRefresh = {
                        dispatchEvent(OrganizationsEvent.Refresh(organizationId!!))
                    },
                    onAddOrganization = {
                        dispatchEvent(OrganizationsEvent.NavigateToOrganizationEditor(null))
                    },
                    onEditOrganization = {
                        dispatchEvent(OrganizationsEvent.NavigateToOrganizationEditor(organizationId))
                    },
                    onCopyContactInfo = {
                        clipboardManager.setText(AnnotatedString(it.value))
                        coroutineScope.launch { snackbarState.showSnackbar(strings.copyMessage) }
                    },
                    onShowAllEmployee = {
                        dispatchEvent(OrganizationsEvent.NavigateToEmployees(checkNotNull(organizationId)))
                    },
                    onShowEmployeeCard = {
                        dispatchEvent(OrganizationsEvent.OpenEmployeeCard(it, checkNotNull(organizationId)))
                    },
                    onShowAllSubjects = {
                        dispatchEvent(OrganizationsEvent.NavigateToSubjects(checkNotNull(organizationId)))
                    },
                    onShowSubjectEditor = {
                        dispatchEvent(OrganizationsEvent.NavigateToSubjectEditor(it, checkNotNull(organizationId)))
                    },
                )
            },
            topBar = {
                OrganizationsTopBar()
            },
            bottomBar = {
                OrganizationsBottomBar(
                    pagerState = pagerState,
                    allOrganizations = state.shortOrganizations,
                    selectedOrganization = state.selectedOrganization,
                    onChangeOrganization = {
                        dispatchEvent(OrganizationsEvent.ChangeOrganization(it?.uid))
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { Snackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is OrganizationsEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is OrganizationsEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is OrganizationsEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}