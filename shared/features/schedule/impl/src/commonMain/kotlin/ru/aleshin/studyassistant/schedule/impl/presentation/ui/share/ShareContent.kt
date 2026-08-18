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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.ads.LocalAdsConfiguration
import ru.aleshin.studyassistant.core.ui.ads.YandexRewardedAdHost
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.ShareCodeScannerDialog
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store.ShareComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareBottomActionBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareTopBar
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.scan_qr_button_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
internal fun ShareContent(
    modifier: Modifier = Modifier,
    shareComponent: ShareComponent,
) {
    val store = shareComponent.store
    val state by store.stateAsState()
    val clipboardManager = LocalClipboardManager.current
    val coreCancelTitle = stringResource(CoreRes.string.core_cancel_title)
    val adsConfiguration = LocalAdsConfiguration.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isScannerOpen by rememberSaveable { mutableStateOf(false) }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchShareLayoutMode()
    val isExpanded = layoutMode == ShareLayoutMode.EXPANDED

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ShareTopBar(
                titleAlign = if (isExpanded) TextAlign.Start else TextAlign.Center,
                onBackClick = { store.dispatchEvent(ShareEvent.ClickBack) },
            )
        },
        bottomBar = {
            if (state.status == ShareStatus.PREVIEW) {
                ShareBottomActionBar(
                    enabled = !state.isRewardInProgress,
                    isLoadingAccept = state.isRewardInProgress,
                    contentMaxWidth = if (isExpanded) {
                        AdaptiveLayoutDefaults.MediumContentMaxWidth
                    } else {
                        null
                    },
                    horizontalPadding = if (isExpanded) {
                        AdaptiveLayoutDefaults.ExpandedHorizontalPadding
                    } else {
                        AdaptiveLayoutDefaults.CompactHorizontalPadding
                    },
                    onAcceptSharedSchedule = {
                        store.dispatchEvent(ShareEvent.AcceptedSharedSchedule)
                    },
                    onRejectSharedSchedule = {
                        store.dispatchEvent(ShareEvent.RejectedSharedSchedule)
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData -> ErrorSnackbar(snackbarData) },
            )
        },
    ) { contentPadding ->
        ShareLayout(
            modifier = Modifier.padding(contentPadding),
            state = state,
            layoutMode = layoutMode,
            onCodeChange = { code -> store.dispatchEvent(ShareEvent.UpdatedCode(code)) },
            onCreateClick = { store.dispatchEvent(ShareEvent.CreateShare) },
            onToggleOrganization = { organizationId ->
                store.dispatchEvent(ShareEvent.ToggleShareOrganization(organizationId))
            },
            onClaimClick = { store.dispatchEvent(ShareEvent.ClaimShare) },
            onScanClick = { isScannerOpen = true },
            onCopyLinkClick = {
                state.link?.let { link ->
                    clipboardManager.setText(AnnotatedString(link.deepLink))
                }
            },
            onResetClick = { store.dispatchEvent(ShareEvent.Reset) },
            onLinkOrganization = { sharedOrganizationId, linkedOrganizationId ->
                store.dispatchEvent(
                    ShareEvent.ClickLinkOrganization(
                        sharedOrganizationId,
                        linkedOrganizationId,
                    )
                )
            },
            onLinkSubjects = { organizationId, subjects ->
                store.dispatchEvent(ShareEvent.UpdatedLinkedSubjects(organizationId, subjects))
            },
            onLinkTeachers = { organizationId, teachers ->
                store.dispatchEvent(ShareEvent.UpdatedLinkedTeachers(organizationId, teachers))
            },
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

    YandexRewardedAdHost(
        adUnitId = adsConfiguration?.scheduleImportRewardedId.orEmpty(),
        requestKey = state.rewardChallengeId,
        onRewarded = { challengeId ->
            store.dispatchEvent(ShareEvent.RewardedAdGranted(challengeId))
        },
        onUnavailable = { store.dispatchEvent(ShareEvent.RewardedAdUnavailable) },
    )

    store.handleEffects { effect ->
        when (effect) {
            is ShareEffect.ShowError -> snackbarHostState.showSnackbar(
                message = effect.failures.mapToMessage(),
                withDismissAction = true,
            )
        }
    }
}
