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

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.event_type_class as core_event_type_class
import ru.aleshin.studyassistant.core.ui.resources.event_type_lecture as core_event_type_lecture
import ru.aleshin.studyassistant.core.ui.resources.event_type_lesson as core_event_type_lesson
import ru.aleshin.studyassistant.core.ui.resources.event_type_online_class as core_event_type_online_class
import ru.aleshin.studyassistant.core.ui.resources.event_type_practice as core_event_type_practice
import ru.aleshin.studyassistant.core.ui.resources.event_type_seminar as core_event_type_seminar
import ru.aleshin.studyassistant.core.ui.resources.event_type_webinar as core_event_type_webinar
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_lecture as core_ic_lecture
import ru.aleshin.studyassistant.core.ui.resources.ic_online_lesson as core_ic_online_lesson
import ru.aleshin.studyassistant.core.ui.resources.ic_practice as core_ic_practice
import ru.aleshin.studyassistant.core.ui.resources.ic_seminar as core_ic_seminar
import ru.aleshin.studyassistant.core.ui.resources.ic_webinar as core_ic_webinar

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
fun EventType.mapToString(): String = when (this) {
    EventType.LESSON -> stringResource(CoreRes.string.core_event_type_lesson)
    EventType.LECTURE -> stringResource(CoreRes.string.core_event_type_lecture)
    EventType.PRACTICE -> stringResource(CoreRes.string.core_event_type_practice)
    EventType.SEMINAR -> stringResource(CoreRes.string.core_event_type_seminar)
    EventType.CLASS -> stringResource(CoreRes.string.core_event_type_class)
    EventType.ONLINE_CLASS -> stringResource(CoreRes.string.core_event_type_online_class)
    EventType.WEBINAR -> stringResource(CoreRes.string.core_event_type_webinar)
}

fun EventType.mapToIcon(): DrawableResource = when (this) {
    EventType.LESSON -> CoreRes.drawable.core_ic_class
    EventType.LECTURE -> CoreRes.drawable.core_ic_lecture
    EventType.PRACTICE -> CoreRes.drawable.core_ic_practice
    EventType.SEMINAR -> CoreRes.drawable.core_ic_seminar
    EventType.CLASS -> CoreRes.drawable.core_ic_class
    EventType.ONLINE_CLASS -> CoreRes.drawable.core_ic_online_lesson
    EventType.WEBINAR -> CoreRes.drawable.core_ic_webinar
}