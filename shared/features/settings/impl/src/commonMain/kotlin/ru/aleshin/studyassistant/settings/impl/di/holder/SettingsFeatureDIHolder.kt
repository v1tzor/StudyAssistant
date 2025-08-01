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

package ru.aleshin.studyassistant.settings.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProductsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.settings.api.di.SettingsFeatureApi
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.settings.impl.di.SettingsFeatureDependencies
import ru.aleshin.studyassistant.settings.impl.di.modules.domainModule
import ru.aleshin.studyassistant.settings.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.settings.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
public object SettingsFeatureDIHolder :
    BaseFeatureDIHolder<SettingsFeatureApi, SettingsFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: SettingsFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> BillingFeatureStarter> { dependencies.billingFeatureStarter }
                bindSingleton<GeneralSettingsRepository> { dependencies.generalSettingsRepository }
                bindSingleton<ProductsRepository> { dependencies.productsRepository }
                bindSingleton<DailyGoalsRepository> { dependencies.goalsRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<NotificationSettingsRepository> { dependencies.notificationSettingsRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworksRepository }
                bindSingleton<TodoRepository> { dependencies.todosRepository }
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<StartClassesReminderManager> { dependencies.startClassesReminderManager }
                bindSingleton<EndClassesReminderManager> { dependencies.endClassesReminderManager }
                bindSingleton<WorkloadWarningManager> { dependencies.workloadWarningManager }
                bindSingleton<HomeworksReminderManager> { dependencies.homeworksReminderManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<IapService> { dependencies.iapService }
                bindSingleton<SettingsFeatureApi> {
                    object : SettingsFeatureApi {
                        override fun fetchStarter() = instance<SettingsFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): SettingsFeatureApi {
        return fetchDI().instance<SettingsFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Settings feature DI is not initialized"
    }
}