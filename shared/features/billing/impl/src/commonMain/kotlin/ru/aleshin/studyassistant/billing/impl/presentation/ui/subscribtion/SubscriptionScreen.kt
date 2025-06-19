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

package ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.billing.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.billing.impl.presentation.theme.BillingThemeRes
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionEffect
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionEvent
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionViewState
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.screenmodel.rememberSubscriptionScreenModel
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.views.SubscriptionBottomBar
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.views.SubscriptionTopBar
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.views.SuccessPaymentDialog
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 17.06.2025
 */
internal class SubscriptionScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberSubscriptionScreenModel(),
        initialState = SubscriptionViewState(),
    ) { state ->
        val strings = BillingThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }
        var successDialogState by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                SubscriptionContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onChooseProduct = { dispatchEvent(SubscriptionEvent.ChooseProduct(it)) }
                )
            },
            topBar = {
                SubscriptionTopBar(
                    enabled = !state.isLoadingPurchase,
                    onBackClick = { dispatchEvent(SubscriptionEvent.NavigateToBack) },
                )
            },
            bottomBar = {
                SubscriptionBottomBar(
                    enabled = !state.isLoadingProducts && state.selectedProduct != null && !state.isPaidUser,
                    isLoadingPurchase = state.isLoadingPurchase,
                    onSubscribe = {
                        if (state.selectedProduct != null) {
                            dispatchEvent(SubscriptionEvent.PurchaseProduct(state.selectedProduct.productId))
                        }
                    },
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
                is SubscriptionEffect.ShowSuccessDialog -> {
                    successDialogState = true
                }
                is SubscriptionEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
                is SubscriptionEffect.NavigateToBack -> navigator.nestedPop()
            }
        }

        if (successDialogState) {
            SuccessPaymentDialog(onDismiss = { dispatchEvent(SubscriptionEvent.NavigateToBack) })
        }
    }
}