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

import ru.aleshin.studyassistant.core.domain.entities.settings.AccessType
import ru.aleshin.studyassistant.core.domain.entities.settings.PrivacySettings
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetwork
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.models.users.SocialNetworkPojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
fun AppUser.mapToRemote() = AppUserPojo(
    uid = uid,
    messageId = messageId,
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender?.name,
    friends = friends,
    socialNetworks = socialNetworks.map { it.mapToRemote() },
    privateProfile = privacy.isPrivateProfile,
    showBirthday = privacy.showBirthday.name,
    showCity = privacy.showCity.name,
)

fun AppUserPojo.mapToDomain() = AppUser(
    messageId = messageId,
    uid = uid,
    username = username,
    email = email,
    code = code,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = gender?.let { Gender.valueOf(it) },
    friends = friends,
    socialNetworks = socialNetworks.map { it.mapToDomain() },
    privacy = PrivacySettings(
        isPrivateProfile = privateProfile,
        showBirthday = AccessType.valueOf(showBirthday),
        showCity = AccessType.valueOf(showCity),
    ),
)

fun SocialNetwork.mapToRemote() = SocialNetworkPojo(
    name = name,
    icon = icon,
    url = url
)

fun SocialNetworkPojo.mapToDomain() = SocialNetwork(
    name = name,
    icon = icon,
    url = url
)