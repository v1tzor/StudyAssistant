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

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantIcons
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
fun EventType.mapToString(strings: StudyAssistantStrings): String = when (this) {
    EventType.LESSON -> strings.eventTypeLesson
    EventType.LECTURE -> strings.eventTypeLecture
    EventType.PRACTICE -> strings.eventTypePractice
    EventType.SEMINAR -> strings.eventTypeSeminar
    EventType.CLASS -> strings.eventTypeClass
    EventType.ONLINE_CLASS -> strings.eventTypeOnlineClass
    EventType.WEBINAR -> strings.eventTypeWebinar
}

@OptIn(ExperimentalResourceApi::class)
fun EventType.mapToIcon(icons: StudyAssistantIcons): DrawableResource = when (this) {
    EventType.LESSON -> icons.lesson
    EventType.LECTURE -> icons.lecture
    EventType.PRACTICE -> icons.practice
    EventType.SEMINAR -> icons.seminar
    EventType.CLASS -> icons.classes
    EventType.ONLINE_CLASS -> icons.onlineClass
    EventType.WEBINAR -> icons.webinar
}