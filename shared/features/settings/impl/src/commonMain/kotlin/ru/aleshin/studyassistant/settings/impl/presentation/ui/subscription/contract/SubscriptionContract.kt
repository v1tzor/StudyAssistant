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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.billing.SubscriptionUi

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
@Serializable
internal data class SubscriptionState(
    val isLoadingSync: Boolean = false,
    val isLoadingSubscriptions: Boolean = false,
    val isPaidUser: Boolean? = null,
    val haveRemoteData: Boolean? = null,
    val currentStore: Store? = null,
    val subscriptions: List<SubscriptionUi> = emptyList(),
) : StoreState

internal sealed class SubscriptionEvent : StoreEvent {
    data object Init : SubscriptionEvent()
    data class TransferRemoteData(val mergeData: Boolean) : SubscriptionEvent()
    data class TransferLocalData(val mergeData: Boolean) : SubscriptionEvent()
    data object RestoreSubscription : SubscriptionEvent()
    data object ControlSubscription : SubscriptionEvent()
    data object NavigateToBilling : SubscriptionEvent()
}

internal sealed class SubscriptionEffect : StoreEffect {
    data class OpenUri(val uri: String) : SubscriptionEffect()
    data class ShowError(val failures: SettingsFailures) : SubscriptionEffect()
    data object SuccessRestoreMessage : SubscriptionEffect()
}

internal sealed class SubscriptionAction : StoreAction {
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : SubscriptionAction()
    data class UpdateRemoteDataStatus(val haveRemoteData: Boolean) : SubscriptionAction()
    data class UpdateSubscriptions(val subscriptions: List<SubscriptionUi>) : SubscriptionAction()
    data class UpdateCurrentStore(val store: Store?) : SubscriptionAction()
    data class UpdateLoadingSubscriptions(val isLoading: Boolean) : SubscriptionAction()
    data class UpdateLoadingSync(val isLoading: Boolean) : SubscriptionAction()
}

internal sealed class SubscriptionOutput : BaseOutput {
    data object NavigateToBilling : SubscriptionOutput()
}