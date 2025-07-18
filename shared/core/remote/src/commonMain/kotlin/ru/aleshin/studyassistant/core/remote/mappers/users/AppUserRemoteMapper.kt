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

package ru.aleshin.studyassistant.core.remote.mappers.users

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails

/**
 * @author Stanislav Aleshin on 06.07.2025.
 */
fun AppUserPojo.convertToDetails() = AppUserPojoDetails(
    uid = uid,
    devices = devices.map { it.fromJson() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    sex = sex,
    friends = friends,
    subscriptionInfo = subscriptionInfo?.fromJson(),
    socialNetworks = socialNetworks.map { it.fromJson() },
)

fun AppUserPojoDetails.convertToBase() = AppUserPojo(
    uid = uid,
    devices = devices.map { it.toJson() },
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    sex = sex,
    friends = friends,
    subscriptionInfo = subscriptionInfo?.toJson(),
    socialNetworks = socialNetworks.map { it.toJson() },
)