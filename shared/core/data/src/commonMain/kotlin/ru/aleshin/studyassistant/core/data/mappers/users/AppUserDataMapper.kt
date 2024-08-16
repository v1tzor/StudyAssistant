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

package ru.aleshin.studyassistant.core.data.mappers.users

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetwork
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetworkType
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.models.users.SocialNetworkPojo
import ru.aleshin.studyassistant.core.remote.models.users.UserDevicePojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
fun AppUser.mapToRemoteData() = AppUserPojo(
    uid = uid,
    devices = devices.map { it.mapToRemoteData() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender?.name,
    friends = friends,
    subscribePeriod = subscribePeriod?.toEpochMilliseconds(),
    socialNetworks = socialNetworks.map { it.mapToRemoteData() },
)

fun AppUserPojo.mapToDomain() = AppUser(
    uid = uid,
    devices = devices.map { it.mapToDomain() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender?.let { Gender.valueOf(it) },
    friends = friends,
    subscribePeriod = subscribePeriod?.mapEpochTimeToInstant(),
    socialNetworks = socialNetworks.map { it.mapToDomain() },
)

internal fun UserDevice.mapToRemoteData() = UserDevicePojo(
    platform = platform,
    deviceId = deviceId,
    deviceName = deviceName,
    pushToken = pushToken,
    pushServiceType = pushServiceType.toString(),
)

internal fun UserDevicePojo.mapToDomain() = UserDevice(
    platform = platform,
    deviceId = deviceId,
    deviceName = deviceName,
    pushToken = pushToken,
    pushServiceType = PushServiceType.valueOf(pushServiceType),
)

fun SocialNetwork.mapToRemoteData() = SocialNetworkPojo(
    type = type.name,
    otherType = otherType,
    data = data,
)

fun SocialNetworkPojo.mapToDomain() = SocialNetwork(
    type = SocialNetworkType.valueOf(type),
    otherType = otherType,
    data = data,
)