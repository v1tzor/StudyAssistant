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

package ru.aleshin.studyassistant.chat.impl.presentation.models.users

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store

/**
 * @author Stanislav Aleshin on 30.08.2024.
 */
@Immutable
@Serializable
internal data class SubscribeInfoUi(
    val uid: UID,
    val deviceId: UID,
    val purchaseId: UID,
    val productId: UID,
    val subscriptionToken: UID? = null,
    val startTimeMillis: Long,
    val expiryTimeMillis: Long,
    val store: Store,
)