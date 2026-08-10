/*
 * Copyright 2026 Stanislav Aleshin
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

import ru.aleshin.studyassistant.core.database.models.users.ProfileEntity
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.domain.entities.users.Profile

fun Profile.mapToLocalData() = ProfileEntity(
    uid = uid,
    username = username,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    sex = gender?.name,
    updatedAt = updatedAt,
)

fun ProfileEntity.mapToDomain() = Profile(
    uid = uid,
    username = username,
    avatar = avatar,
    description = description,
    city = city,
    birthday = birthday,
    gender = sex?.let(Gender::valueOf),
    updatedAt = updatedAt,
)
