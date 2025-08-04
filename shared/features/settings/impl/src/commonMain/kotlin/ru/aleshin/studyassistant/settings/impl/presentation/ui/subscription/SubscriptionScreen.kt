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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionViewState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.screenmodel.rememberSubscriptionScreenModel

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
internal class SubscriptionScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberSubscriptionScreenModel(),
        initialState = SubscriptionViewState(),
    ) { state ->
        val strings = SettingsThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                SubscriptionContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onTransferRemoteData = { dispatchEvent(SubscriptionEvent.TransferRemoteData(it)) },
                    onTransferLocalData = { dispatchEvent(SubscriptionEvent.TransferLocalData(it)) },
                    onOpenBilling = { dispatchEvent(SubscriptionEvent.NavigateToBilling) },
                    onControlSubscription = { dispatchEvent(SubscriptionEvent.ControlSubscription) },
                    onRestoreSubscription = { dispatchEvent(SubscriptionEvent.RestoreSubscription) },
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

        handleEffect { effect ->
            when (effect) {
                is SubscriptionEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
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
}