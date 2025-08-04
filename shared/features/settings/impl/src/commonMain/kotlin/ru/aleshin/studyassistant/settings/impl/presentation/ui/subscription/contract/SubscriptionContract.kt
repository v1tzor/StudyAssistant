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

import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.billing.SubscriptionUi

/**
 * @author Stanislav Aleshin on 28.08.2024
 */
@Parcelize
internal data class SubscriptionViewState(
    val isLoadingSync: Boolean = false,
    val isLoadingSubscriptions: Boolean = false,
    val isPaidUser: Boolean? = null,
    val haveRemoteData: Boolean? = null,
    val currentStore: Store? = null,
    val subscriptions: List<SubscriptionUi> = emptyList(),
) : BaseViewState

internal sealed class SubscriptionEvent : BaseEvent {
    data object Init : SubscriptionEvent()
    data class TransferRemoteData(val mergeData: Boolean) : SubscriptionEvent()
    data class TransferLocalData(val mergeData: Boolean) : SubscriptionEvent()
    data object RestoreSubscription : SubscriptionEvent()
    data object ControlSubscription : SubscriptionEvent()
    data object NavigateToBilling : SubscriptionEvent()
}

internal sealed class SubscriptionEffect : BaseUiEffect {
    data class NavigateToGlobal(val pushScreen: Screen) : SubscriptionEffect()
    data class OpenUri(val uri: String) : SubscriptionEffect()
    data class ShowError(val failures: SettingsFailures) : SubscriptionEffect()
    data object SuccessRestoreMessage : SubscriptionEffect()
}

internal sealed class SubscriptionAction : BaseAction {
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : SubscriptionAction()
    data class UpdateRemoteDataStatus(val haveRemoteData: Boolean) : SubscriptionAction()
    data class UpdateSubscriptions(val subscriptions: List<SubscriptionUi>) : SubscriptionAction()
    data class UpdateCurrentStore(val store: Store?) : SubscriptionAction()
    data class UpdateLoadingSubscriptions(val isLoading: Boolean) : SubscriptionAction()
    data class UpdateLoadingSync(val isLoading: Boolean) : SubscriptionAction()
}