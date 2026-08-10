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

import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTaskBucket
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTaskDistribution
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTaskBucketUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTaskDistributionUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal fun AnalyticsTaskDistribution.mapToUi() = AnalyticsTaskDistributionUi(
    summary = summary.mapToUi(),
    buckets = buckets.map { it.mapToUi() },
    testsCount = testsCount,
    theoreticalPartsCount = theoreticalPartsCount,
    practicalPartsCount = practicalPartsCount,
    presentationPartsCount = presentationPartsCount,
    standardTodos = standardTodos,
    mediumPriorityTodos = mediumPriorityTodos,
    highPriorityTodos = highPriorityTodos,
)

internal fun AnalyticsTaskBucket.mapToUi() = AnalyticsTaskBucketUi(
    from = from,
    to = to,
    completedHomeworks = completedHomeworks,
    completedTodos = completedTodos,
)
