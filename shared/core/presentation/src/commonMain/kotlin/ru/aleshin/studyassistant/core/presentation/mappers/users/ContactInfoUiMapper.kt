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

package ru.aleshin.studyassistant.core.presentation.mappers.users

import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun ContactInfo.mapToUi() = ContactInfoUi(
    label = label,
    value = value,
)

fun ContactInfoUi.mapToDomain() = ContactInfo(
    label = label,
    value = value,
)
