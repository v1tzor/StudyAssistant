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

package ru.aleshin.studyassistant.info.impl.presentation.models.users

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.users.Gender

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class AppUserUi(
    val uid: UID,
    val devices: List<UserDeviceUi>,
    val username: String,
    val email: String,
    val code: String,
    val avatar: String? = null,
    val description: String? = null,
    val city: String? = null,
    val birthday: String? = null,
    val gender: Gender? = null,
    val subscriptionInfo: SubscribeInfoUi? = null,
    val socialNetworks: List<SocialNetworkUi> = emptyList(),
    val friends: List<UID> = emptyList(),
) : Parcelable