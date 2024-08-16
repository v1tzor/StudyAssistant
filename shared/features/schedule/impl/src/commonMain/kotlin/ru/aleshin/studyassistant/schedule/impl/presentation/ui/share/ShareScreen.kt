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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareDeps
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.screenmodel.rememberShareScreenModel
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareBottomActionBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareTopBar

/**
 * @author Stanislav Aleshin on 16.08.2024
 */
internal data class ShareScreen(val receivedShareId: UID) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberShareScreenModel(),
        initialState = ShareViewState(),
        dependencies = ShareDeps(shareId = receivedShareId),
    ) { state ->
        val strings = ScheduleThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                ShareContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onOpenProfile = { dispatchEvent(ShareEvent.NavigateToUserProfile(it)) },
                    onLinkOrganization = { sharedOrganization, linkedOrganization ->
                        dispatchEvent(ShareEvent.LinkOrganization(sharedOrganization, linkedOrganization))
                    },
                    onLinkSubjects = { sharedOrganization, subjects ->
                        dispatchEvent(ShareEvent.UpdateLinkedSubjects(sharedOrganization, subjects))
                    },
                    onLinkTeachers = { sharedOrganization, teachers ->
                        dispatchEvent(ShareEvent.UpdateLinkedTeachers(sharedOrganization, teachers))
                    },
                )
            },
            topBar = {
                ShareTopBar(
                    onBackClick = { dispatchEvent(ShareEvent.NavigateToBack) }
                )
            },
            bottomBar = {
                ShareBottomActionBar(
                    enabled = !state.isLoading && !state.isLoadingAccept,
                    isLoadingAccept = state.isLoadingAccept,
                    onAcceptSharedSchedule = { dispatchEvent(ShareEvent.AcceptSharedSchedule) },
                    onRejectSharedSchedule = { dispatchEvent(ShareEvent.RejectSharedSchedule) },
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is ShareEffect.NavigateToBack -> navigator.nestedPop()
                is ShareEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is ShareEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}