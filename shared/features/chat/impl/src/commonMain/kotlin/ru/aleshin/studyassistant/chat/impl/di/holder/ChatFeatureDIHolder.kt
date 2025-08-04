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

package ru.aleshin.studyassistant.chat.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.chat.api.di.ChatFeatureApi
import ru.aleshin.studyassistant.chat.api.navigation.ChatFeatureStarter
import ru.aleshin.studyassistant.chat.impl.di.ChatFeatureDependencies
import ru.aleshin.studyassistant.chat.impl.di.modules.domainModule
import ru.aleshin.studyassistant.chat.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.chat.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.managers.TimeOverlayManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyAiStatisticsRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
public object ChatFeatureDIHolder : BaseFeatureDIHolder<ChatFeatureApi, ChatFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: ChatFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> BillingFeatureStarter> { dependencies.billingFeatureStarter }
                bindSingleton<AiAssistantRepository> { dependencies.aiAssistantRepository }
                bindSingleton<DailyAiStatisticsRepository> { dependencies.dailyAiStatisticsRepository }
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<DailyGoalsRepository> { dependencies.goalsRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworksRepository }
                bindSingleton<TodoRepository> { dependencies.todoRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<NotificationSettingsRepository> { dependencies.notificationSettingsRepository }
                bindSingleton<StartClassesReminderManager> { dependencies.startClassesReminderManager }
                bindSingleton<EndClassesReminderManager> { dependencies.endClassesReminderManager }
                bindSingleton<TodoReminderManager> { dependencies.todoReminderManager }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<ManageUserRepository> { dependencies.manageUserRepository }
                bindSingleton<TimeOverlayManager> { dependencies.overlayManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<ChatFeatureApi> {
                    object : ChatFeatureApi {
                        override fun fetchStarter() = instance<ChatFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): ChatFeatureApi {
        return fetchDI().instance<ChatFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Chat feature DI is not initialized"
    }
}