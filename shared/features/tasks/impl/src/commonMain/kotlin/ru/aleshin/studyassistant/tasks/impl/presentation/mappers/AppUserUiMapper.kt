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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetwork
import ru.aleshin.studyassistant.core.domain.entities.users.SubscribeInfo
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.SocialNetworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.SubscribeInfoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.UserDeviceUi

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal fun AppUser.mapToUi() = AppUserUi(
    uid = uid,
    devices = devices.map { it.mapToUi() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender,
    friends = friends,
    subscriptionInfo = subscriptionInfo?.mapToUi(),
    socialNetworks = socialNetworks.map { it.mapToUi() },
)

internal fun AppUserUi.mapToDomain() = AppUser(
    uid = uid,
    devices = devices.map { it.mapToDomain() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender,
    friends = friends,
    subscriptionInfo = subscriptionInfo?.mapToDomain(),
    socialNetworks = socialNetworks.map { it.mapToDomain() },
)

internal fun SubscribeInfo.mapToUi() = SubscribeInfoUi(
    deviceId = deviceId,
    purchaseId = purchaseId,
    productId = productId,
    subscriptionToken = subscriptionToken,
    purchaseDate = purchaseDate,
    subscriptionPeriod = subscriptionPeriod,
    store = store,
)

internal fun SubscribeInfoUi.mapToDomain() = SubscribeInfo(
    deviceId = deviceId,
    purchaseId = purchaseId,
    productId = productId,
    subscriptionToken = subscriptionToken,
    purchaseDate = purchaseDate,
    subscriptionPeriod = subscriptionPeriod,
    store = store,
)

internal fun UserDevice.mapToUi() = UserDeviceUi(
    platform = platform,
    deviceId = deviceId,
    deviceName = deviceName,
    pushToken = pushToken,
    pushServiceType = pushServiceType,
)

internal fun UserDeviceUi.mapToDomain() = UserDevice(
    platform = platform,
    deviceId = deviceId,
    deviceName = deviceName,
    pushToken = pushToken,
    pushServiceType = pushServiceType,
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