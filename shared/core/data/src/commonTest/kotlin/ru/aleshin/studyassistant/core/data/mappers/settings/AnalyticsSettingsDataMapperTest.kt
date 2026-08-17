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
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class AnalyticsSettingsDataMapperTest {

    @Test
    fun mapsKnownPeriodAndCustomRange() {
        val entity = AnalyticsSettingsEntity(
            id = 1L,
            period = "CUSTOM",
            custom_from = 10L,
            custom_to = 20L,
        )

        val settings = entity.mapToDomain()

        assertEquals(AnalyticsPeriod.CUSTOM, settings.period)
        assertEquals(10L, settings.customFrom)
        assertEquals(20L, settings.customTo)
    }

    @Test
    fun unknownPeriodFallsBackToMonth() {
        val entity = AnalyticsSettingsEntity(
            id = 1L,
            period = "QUARTER",
            custom_from = null,
            custom_to = null,
        )

        assertEquals(AnalyticsPeriod.MONTH, entity.mapToDomain().period)
    }

    @Test
    fun mapsDomainBackToLocalRow() {
        val local = AnalyticsSettings(
            period = AnalyticsPeriod.WEEK,
            customFrom = null,
            customTo = null,
        ).mapToLocalData()

        assertEquals(1L, local.id)
        assertEquals("WEEK", local.period)
        assertEquals(null, local.custom_from)
        assertEquals(null, local.custom_to)
    }
}
