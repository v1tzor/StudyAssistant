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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.settings.PrivacySettings
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetwork
import ru.aleshin.studyassistant.schedule.impl.presentation.models.settings.PrivacySettingsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SocialNetworkUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.AppUserUi

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
    socialNetworks = socialNetworks.map { it.mapToUi() },
    privacy = privacy.mapToUi(),
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
    socialNetworks = socialNetworks.map { it.mapToDomain() },
    privacy = privacy.mapToDomain(),
)

internal fun PrivacySettings.mapToUi() = PrivacySettingsUi(
    isPrivateProfile = isPrivateProfile,
    showBirthday = showBirthday,
    showCity = showCity,
)

internal fun PrivacySettingsUi.mapToDomain() = PrivacySettings(
    isPrivateProfile = isPrivateProfile,
    showBirthday = showBirthday,
    showCity = showCity
)

internal fun SocialNetwork.mapToUi() = SocialNetworkUi(
    name = name,
    icon = icon,
    url = url,
)

internal fun SocialNetworkUi.mapToDomain() = SocialNetwork(
    name = name,
    icon = icon,
    url = url,
)
