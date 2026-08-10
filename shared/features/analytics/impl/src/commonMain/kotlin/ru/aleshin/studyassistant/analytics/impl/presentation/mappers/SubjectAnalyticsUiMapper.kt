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

package ru.aleshin.studyassistant.analytics.impl.presentation.mappers

import ru.aleshin.studyassistant.analytics.impl.domain.entities.SubjectAnalytics
import ru.aleshin.studyassistant.analytics.impl.presentation.models.SubjectAnalyticsUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal fun SubjectAnalytics.mapToUi() = SubjectAnalyticsUi(
    subject = subject?.mapToUi(),
    plannedDuration = plannedDuration,
    classesCount = classesCount,
    homeworkCount = homeworkCount,
    testsCount = testsCount,
    completedOnTime = completedOnTime,
    overdue = overdue,
)
