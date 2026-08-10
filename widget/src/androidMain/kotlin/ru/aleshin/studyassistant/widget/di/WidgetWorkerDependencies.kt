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

package ru.aleshin.studyassistant.widget.di

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindProvider
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.MainDependenciesGraph
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
object WidgetWorkerDependencies {

    fun create(builder: DI.Builder.() -> Unit = {}): DirectDI = DI.direct {
        bindProvider<BaseScheduleRepository> { mainInstance() }
        bindProvider<CustomScheduleRepository> { mainInstance() }
        bindProvider<CalendarSettingsRepository> { mainInstance() }
        bindProvider<HomeworksRepository> { mainInstance() }
        bindProvider<TodoRepository> { mainInstance() }
        bindProvider<DailyGoalsRepository> { mainInstance() }
        bindProvider<GeneralSettingsRepository> { mainInstance() }
        bindProvider<TodoReminderManager> { mainInstance() }
        bindProvider<DateManager> { mainInstance() }
        bindProvider<CrashlyticsService> { mainInstance() }
        import(widgetModule)
        builder()
    }
}

private inline fun <reified T : Any> mainInstance(): T {
    return MainDependenciesGraph.fetchDI().instance()
}
