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

package ru.aleshin.studyassistant.core.ui.mappers

import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfoType
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_email as core_ic_email
import ru.aleshin.studyassistant.core.ui.resources.ic_map_marker as core_ic_map_marker
import ru.aleshin.studyassistant.core.ui.resources.ic_phone as core_ic_phone
import ru.aleshin.studyassistant.core.ui.resources.ic_web as core_ic_web

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
fun ContactInfoType.mapToIcon() = when (this) {
    ContactInfoType.EMAIL -> CoreRes.drawable.core_ic_email
    ContactInfoType.PHONE -> CoreRes.drawable.core_ic_phone
    ContactInfoType.LOCATION -> CoreRes.drawable.core_ic_map_marker
    ContactInfoType.WEBSITE -> CoreRes.drawable.core_ic_web
}