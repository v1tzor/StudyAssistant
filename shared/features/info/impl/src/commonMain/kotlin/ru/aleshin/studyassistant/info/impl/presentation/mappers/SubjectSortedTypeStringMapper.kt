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

package ru.aleshin.studyassistant.info.impl.presentation.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.info.impl.domain.entities.SubjectSortedType
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.alphabetic_sorted_type
import ru.aleshin.studyassistant.info.impl.resources.event_type_sorted_type
import ru.aleshin.studyassistant.info.impl.resources.location_sorted_type
import ru.aleshin.studyassistant.info.impl.resources.office_sorted_type
import ru.aleshin.studyassistant.info.impl.resources.teacher_sorted_type

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun SubjectSortedType.mapToString() = when (this) {
    SubjectSortedType.ALPHABETIC -> stringResource(Res.string.alphabetic_sorted_type)
    SubjectSortedType.TEACHER -> stringResource(Res.string.teacher_sorted_type)
    SubjectSortedType.EVENT_TYPE -> stringResource(Res.string.event_type_sorted_type)
    SubjectSortedType.OFFICE -> stringResource(Res.string.office_sorted_type)
    SubjectSortedType.LOCATION -> stringResource(Res.string.location_sorted_type)
}
