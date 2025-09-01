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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store.SubscriptionComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.views.ActiveSubscriptionView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.views.SyncRemoteDataView

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
@Composable
internal fun SubscriptionContent(
    subscriptionComponent: SubscriptionComponent,
    modifier: Modifier = Modifier,
) {
    val store = subscriptionComponent.store
    val state by store.stateAsState()
    val strings = SettingsThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val uriHandler = LocalUriHandler.current
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            SubscriptionContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onTransferRemoteData = { store.dispatchEvent(SubscriptionEvent.TransferRemoteData(it)) },
                onTransferLocalData = { store.dispatchEvent(SubscriptionEvent.TransferLocalData(it)) },
                onOpenBilling = { store.dispatchEvent(SubscriptionEvent.NavigateToBilling) },
                onControlSubscription = { store.dispatchEvent(SubscriptionEvent.ControlSubscription) },
                onRestoreSubscription = { store.dispatchEvent(SubscriptionEvent.RestoreSubscription) },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { Snackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is SubscriptionEffect.OpenUri -> uriHandler.openUri(effect.uri)
            is SubscriptionEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
            is SubscriptionEffect.SuccessRestoreMessage -> {
                snackbarState.showSnackbar(
                    message = strings.successRestoreSubscriptionTitle,
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun SubscriptionContent(
    state: SubscriptionState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onTransferRemoteData: (mergeData: Boolean) -> Unit,
    onTransferLocalData: (mergeData: Boolean) -> Unit,
    onOpenBilling: () -> Unit,
    onControlSubscription: () -> Unit,
    onRestoreSubscription: () -> Unit,
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ActiveSubscriptionView(
            modifier = Modifier.padding(horizontal = 16.dp),
            isLoading = state.isLoadingSubscriptions,
            currentStore = state.currentStore,
            subscriptions = state.subscriptions,
            onOpenBillingScreen = onOpenBilling,
            onControlSubscription = onControlSubscription,
            onRestoreSubscription = onRestoreSubscription,
        )
        SyncRemoteDataView(
            modifier = Modifier.padding(horizontal = 16.dp),
            isLoadingSync = state.isLoadingSync,
            isPaidUser = state.isPaidUser,
            haveRemoteData = state.haveRemoteData,
            onTransferRemoteData = onTransferRemoteData,
            onTransferLocalData = onTransferLocalData,
        )
    }
}