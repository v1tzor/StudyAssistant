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

package ru.aleshin.studyassistant.core.database.mappers.user

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.database.models.users.AppUserDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.BaseAppUserEntity
import ru.aleshin.studyassistant.core.database.models.users.SocialNetworkEntity
import ru.aleshin.studyassistant.core.database.models.users.SubscribeInfoEntity
import ru.aleshin.studyassistant.core.database.models.users.UserDeviceEntity
import ru.aleshin.studyassistant.sqldelight.user.CurrentUserEntity

/**
 * @author Stanislav Aleshin on 20.07.2025.
 */
fun CurrentUserEntity.mapToBase() = BaseAppUserEntity(
    uid = document_id,
    devices = devices,
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    sex = sex,
    friends = friends,
    subscriptionInfo = subscription_info,
    socialNetworks = social_networks,
    updatedAt = updated_at,
)

fun BaseAppUserEntity.mapToEntity() = CurrentUserEntity(
    id = 1,
    document_id = uid,
    devices = devices,
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    sex = sex,
    friends = friends,
    subscription_info = subscriptionInfo,
    social_networks = socialNetworks,
    updated_at = updatedAt,
)

fun BaseAppUserEntity.convertToDetails() = AppUserDetailsEntity(
    uid = uid,
    devices = devices.map { it.fromJson<UserDeviceEntity>() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    sex = sex,
    friends = friends,
    subscriptionInfo = subscriptionInfo?.fromJson<SubscribeInfoEntity>(),
    socialNetworks = socialNetworks.map { it.fromJson<SocialNetworkEntity>() },
    updatedAt = updatedAt,
)