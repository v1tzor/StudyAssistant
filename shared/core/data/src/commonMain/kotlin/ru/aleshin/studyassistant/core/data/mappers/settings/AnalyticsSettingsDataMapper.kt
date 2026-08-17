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

package ru.aleshin.studyassistant.core.data.mappers.settings

import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsPeriod
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsSettings
import ru.aleshin.studyassistant.sqldelight.settings.AnalyticsSettingsEntity

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
fun AnalyticsSettingsEntity.mapToDomain() = AnalyticsSettings(
    period = AnalyticsPeriod.entries.firstOrNull { entry -> entry.name == period } ?: AnalyticsPeriod.MONTH,
    customFrom = custom_from,
    customTo = custom_to,
)

fun AnalyticsSettings.mapToLocalData() = AnalyticsSettingsEntity(
    id = 1L,
    period = period.name,
    custom_from = customFrom,
    custom_to = customTo,
)
