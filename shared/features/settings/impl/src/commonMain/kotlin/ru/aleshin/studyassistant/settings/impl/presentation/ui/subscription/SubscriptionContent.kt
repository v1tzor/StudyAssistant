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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionViewState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.views.ActiveSubscriptionView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.views.SyncRemoteDataView

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
@Composable
internal fun SubscriptionContent(
    state: SubscriptionViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onTransferRemoteData: (mergeData: Boolean) -> Unit,
    onTransferLocalData: (mergeData: Boolean) -> Unit,
    onOpenBilling: () -> Unit,
    onControlSubscription: () -> Unit,
    onRestoreSubscription: () -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ActiveSubscriptionView(
            modifier = Modifier.padding(horizontal = 16.dp),
            isLoading = isLoadingSubscriptions,
            currentStore = currentStore,
            subscriptions = subscriptions,
            onOpenBillingScreen = onOpenBilling,
            onControlSubscription = onControlSubscription,
            onRestoreSubscription = onRestoreSubscription,
        )
        SyncRemoteDataView(
            modifier = Modifier.padding(horizontal = 16.dp),
            isLoadingSync = isLoadingSync,
            isPaidUser = isPaidUser,
            haveRemoteData = haveRemoteData,
            onTransferRemoteData = onTransferRemoteData,
            onTransferLocalData = onTransferLocalData,
        )
    }
}