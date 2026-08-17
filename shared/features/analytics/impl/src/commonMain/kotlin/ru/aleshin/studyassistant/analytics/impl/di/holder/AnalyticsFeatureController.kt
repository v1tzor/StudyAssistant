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

package ru.aleshin.studyassistant.analytics.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.analytics.api.AnalyticsContentProviderFactory
import ru.aleshin.studyassistant.analytics.api.AnalyticsFeatureApi
import ru.aleshin.studyassistant.analytics.impl.di.AnalyticsFeatureDependencies
import ru.aleshin.studyassistant.analytics.impl.di.modules.domainModule
import ru.aleshin.studyassistant.analytics.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureController
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.AnalyticsSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
public class AnalyticsFeatureController(
    dependencies: AnalyticsFeatureDependencies,
) : BaseFeatureController<AnalyticsFeatureApi, AnalyticsFeatureDependencies>(dependencies) {

    override fun DI.MainBuilder.buildDIGraph(dependencies: AnalyticsFeatureDependencies) {
        importAll(presentationModule, domainModule)

        bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
        bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
        bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
        bindSingleton<AnalyticsSettingsRepository> { dependencies.analyticsSettingsRepository }
        bindSingleton<NotificationSettingsRepository> { dependencies.notificationSettingsRepository }
        bindSingleton<HomeworksRepository> { dependencies.homeworksRepository }
        bindSingleton<TodoRepository> { dependencies.todoRepository }
        bindSingleton<DailyGoalsRepository> { dependencies.goalsRepository }
        bindSingleton<DateManager> { dependencies.dateManager }
        bindSingleton<CoroutineManager> { dependencies.coroutineManager }
        bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

        bindSingleton<AnalyticsFeatureApi> {
            object : AnalyticsFeatureApi {
                override fun contentProviderFactory(): AnalyticsContentProviderFactory = instance()
            }
        }
    }

    override fun fetchApi(): AnalyticsFeatureApi = directDI.instance()

    internal fun fetchDI() = directDI
}
