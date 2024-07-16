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

package ru.aleshin.studyassistant.users.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetwork
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.models.SocialNetworkUi

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal fun AppUser.mapToUi() = AppUserUi(
    uid = uid,
    messageId = messageId,
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender,
    friends = friends,
    subscribePeriod = subscribePeriod,
    socialNetworks = socialNetworks.map { it.mapToUi() },
)

internal fun AppUserUi.mapToDomain() = AppUser(
    uid = uid,
    messageId = messageId,
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender,
    friends = friends,
    subscribePeriod = subscribePeriod,
    socialNetworks = socialNetworks.map { it.mapToDomain() },
)

internal fun SocialNetwork.mapToUi() = SocialNetworkUi(
    type = type,
    otherType = otherType,
    data = data,
)

internal fun SocialNetworkUi.mapToDomain() = SocialNetwork(
    type = type,
    otherType = otherType,
    data = data,
)