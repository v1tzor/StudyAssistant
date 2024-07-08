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

import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.remote.models.users.ContactInfoPojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun ContactInfo.mapToRemoteData() = ContactInfoPojo(
    label = label,
    value = value,
)

fun ContactInfo.mapToLocalData() = ContactInfoEntity(
    label = label,
    value = value,
)

fun ContactInfoPojo.mapToDomain() = ContactInfo(
    label = label,
    value = value,
)

fun ContactInfoEntity.mapToDomain() = ContactInfo(
    label = label,
    value = value,
)