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

package ru.aleshin.studyassistant.analytics.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsGoalCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsRangeCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsReportCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsScheduleCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.common.AnalyticsEitherWrapper
import ru.aleshin.studyassistant.analytics.impl.domain.common.AnalyticsErrorHandler
import ru.aleshin.studyassistant.analytics.impl.domain.interactors.AnalyticsInteractor

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal val domainModule = DI.Module("AnalyticsDomain") {
    bindSingleton<AnalyticsErrorHandler> { AnalyticsErrorHandler.Base() }
    bindSingleton<AnalyticsEitherWrapper> { AnalyticsEitherWrapper.Base(instance(), instance()) }
    bindSingleton<AnalyticsRangeCalculator> { AnalyticsRangeCalculator.Base() }
    bindSingleton<AnalyticsScheduleCalculator> { AnalyticsScheduleCalculator.Base() }
    bindSingleton<AnalyticsReportCalculator> { AnalyticsReportCalculator.Base() }
    bindSingleton<AnalyticsGoalCalculator> { AnalyticsGoalCalculator.Base() }
    bindSingleton<AnalyticsInteractor> {
        AnalyticsInteractor.Base(
            baseScheduleRepository = instance(),
            customScheduleRepository = instance(),
            calendarSettingsRepository = instance(),
            notificationSettingsRepository = instance(),
            homeworksRepository = instance(),
            todoRepository = instance(),
            goalsRepository = instance(),
            rangeCalculator = instance(),
            scheduleCalculator = instance(),
            reportCalculator = instance(),
            goalCalculator = instance(),
            dateManager = instance(),
            eitherWrapper = instance(),
        )
    }
}
