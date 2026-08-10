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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.ShareCodeScannerDialog
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.MediatedHomeworksLinkerBottomSheet
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.ShareTopBar
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.scan_qr_button_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ShareContent(
    modifier: Modifier = Modifier,
    shareComponent: ShareComponent,
) {
    val store = shareComponent.store
    val state by store.stateAsState()
    val coreCancelTitle = stringResource(CoreRes.string.core_cancel_title)
    val snackbarHostState = remember { SnackbarHostState() }
    var isScannerOpen by rememberSaveable { mutableStateOf(false) }
    var isLinkerOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ShareTopBar(onBackClick = { store.dispatchEvent(ShareEvent.BackClick) })
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData -> ErrorSnackbar(snackbarData) },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { contentPadding ->
        ShareLayout(
            modifier = Modifier.padding(contentPadding),
            state = state,
            onCodeChange = { code -> store.dispatchEvent(ShareEvent.UpdatedCode(code)) },
            onOpenClick = { store.dispatchEvent(ShareEvent.FetchShare) },
            onScanClick = { isScannerOpen = true },
            onLinkRequest = { isLinkerOpen = true },
            onResetClick = { store.dispatchEvent(ShareEvent.Reset) },
        )
    }

    if (isScannerOpen) {
        ShareCodeScannerDialog(
            title = stringResource(Res.string.scan_qr_button_title),
            cancelTitle = coreCancelTitle,
            onResult = { scannedValue ->
                val code = scannedValue.substringAfter("code=", scannedValue).substringBefore('&')
                isScannerOpen = false
                store.dispatchEvent(ShareEvent.ScannedCode(code))
            },
            onDismiss = { isScannerOpen = false },
        )
    }

    if (isLinkerOpen && state.status == HomeworkShareStatus.PREVIEW) {
        MediatedHomeworksLinkerBottomSheet(
            isLoading = false,
            organizations = state.organizations,
            linkDataList = state.linkDataList,
            linkSchedule = state.linkSchedule,
            linkSubjects = state.linkSubjects,
            onDismissRequest = { isLinkerOpen = false },
            onAddSubject = { organizationId ->
                store.dispatchEvent(ShareEvent.ClickEditSubject(null, organizationId))
            },
            onLoadSubjects = { organizationId ->
                store.dispatchEvent(ShareEvent.LoadLinkSubjects(organizationId))
            },
            onUpdateLinkData = { linkData ->
                store.dispatchEvent(ShareEvent.UpdateLinkData(linkData))
            },
            onAdd = {
                isLinkerOpen = false
                store.dispatchEvent(ShareEvent.AcceptHomework)
            },
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is ShareEffect.ShowError -> snackbarHostState.showSnackbar(
                message = effect.failures.mapToMessage(),
                withDismissAction = true,
            )
        }
    }
}
