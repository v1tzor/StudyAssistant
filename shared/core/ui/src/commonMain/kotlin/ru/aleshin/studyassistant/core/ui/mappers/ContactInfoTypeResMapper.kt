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

package ru.aleshin.studyassistant.core.ui.mappers

import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfoType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantIcons

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
fun ContactInfoType.mapToIcon(icons: StudyAssistantIcons) = when (this) {
    ContactInfoType.EMAIL -> icons.email
    ContactInfoType.PHONE -> icons.phone
    ContactInfoType.LOCATION -> icons.location
    ContactInfoType.WEBSITE -> icons.website
}