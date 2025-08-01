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

package ru.aleshin.studyassistant.di.modules

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindProvider
import org.kodein.di.instance
import org.kodein.di.provider
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.impl.di.AuthFeatureDependencies
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.billing.impl.di.BillingFeatureDependencies
import ru.aleshin.studyassistant.billing.impl.di.holder.BillingFeatureDIHolder
import ru.aleshin.studyassistant.chat.api.navigation.ChatFeatureStarter
import ru.aleshin.studyassistant.chat.impl.di.ChatFeatureDependencies
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureDIHolder
import ru.aleshin.studyassistant.core.api.auth.AccountService
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.managers.TimeOverlayManager
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.common.platform.services.AppService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.managers.sync.SourceSyncFacade
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProductsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.impl.di.EditorFeatureDependencies
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.info.impl.di.InfoFeatureDependencies
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureDIHolder
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.navigation.impl.di.NavigationFeatureDependencies
import ru.aleshin.studyassistant.navigation.impl.di.holder.NavigationFeatureDIHolder
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.profile.impl.di.ProfileFeatureDependencies
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureDIHolder
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.impl.di.ScheduleFeatureDependencies
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.settings.impl.di.SettingsFeatureDependencies
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.impl.di.TasksFeatureDependencies
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter
import ru.aleshin.studyassistant.users.impl.di.UsersFeatureDependencies
import ru.aleshin.studyassistant.users.impl.di.holder.UsersFeatureDIHolder

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
val featureModule = DI.Module("Feature") {
    bindEagerSingleton<NavigationFeatureDependencies> {
        object : NavigationFeatureDependencies {
            override val scheduleFeatureStarter = provider<ScheduleFeatureStarter>()
            override val tasksFeatureStarter = provider<TasksFeatureStarter>()
            override val chatFeatureStarter = provider<ChatFeatureStarter>()
            override val infoFeatureStarter = provider<InfoFeatureStarter>()
            override val profileFeatureStarter = provider<ProfileFeatureStarter>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<NavigationFeatureStarter> {
        with(NavigationFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<PreviewFeatureDependencies> {
        object : PreviewFeatureDependencies {
            override val authFeatureStarter = provider<AuthFeatureStarter>()
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val navigationFeatureStarter = provider<NavigationFeatureStarter>()
            override val billingFeatureStarter = provider<BillingFeatureStarter>()
            override val usersRepository = instance<UsersRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val generalSettingsRepository = instance<GeneralSettingsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<PreviewFeatureStarter> {
        with(PreviewFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<AuthFeatureDependencies> {
        object : AuthFeatureDependencies {
            override val navigationFeatureStarter = provider<NavigationFeatureStarter>()
            override val previewFeatureStarter = provider<PreviewFeatureStarter>()
            override val authRepository = instance<AuthRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val generalSettingsRepository = instance<GeneralSettingsRepository>()
            override val messageRepository = instance<MessageRepository>()
            override val manageUserRepository = instance<ManageUserRepository>()
            override val accountService = instance<AccountService>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val coroutineManager = instance<CoroutineManager>()
            override val sourceSyncFacade = instance<SourceSyncFacade>()
            override val appService = instance<AppService>()
            override val dateManager = instance<DateManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<AuthFeatureStarter> {
        with(AuthFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<ScheduleFeatureDependencies> {
        object : ScheduleFeatureDependencies {
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val usersFeatureStarter = provider<UsersFeatureStarter>()
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val shareSchedulesRepository = instance<ShareSchedulesRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val homeworkRepository = instance<HomeworksRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val startClassesReminderManager = instance<StartClassesReminderManager>()
            override val endClassesReminderManager = instance<EndClassesReminderManager>()
            override val usersRepository = instance<UsersRepository>()
            override val connectionManager = instance<Konnection>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<ScheduleFeatureStarter> {
        with(ScheduleFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<TasksFeatureDependencies> {
        object : TasksFeatureDependencies {
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val usersFeatureStarter = provider<UsersFeatureStarter>()
            override val billingFeatureStarter = provider<BillingFeatureStarter>()
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val homeworkRepository = instance<HomeworksRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val shareHomeworksRepository = instance<ShareHomeworksRepository>()
            override val messageRepository = instance<MessageRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val todoReminderManager = instance<TodoReminderManager>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val connectionManager = instance<Konnection>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<TasksFeatureStarter> {
        with(TasksFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<InfoFeatureDependencies> {
        object : InfoFeatureDependencies {
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val usersFeatureStarter = provider<UsersFeatureStarter>()
            override val billingFeatureStarter = provider<BillingFeatureStarter>()
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<InfoFeatureStarter> {
        with(InfoFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<ProfileFeatureDependencies> {
        object : ProfileFeatureDependencies {
            override val authFeatureStarter = provider<AuthFeatureStarter>()
            override val usersFeatureStarter = provider<UsersFeatureStarter>()
            override val settingsFeatureStarter = provider<SettingsFeatureStarter>()
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val scheduleFeatureStarter = provider<ScheduleFeatureStarter>()
            override val authRepository = instance<AuthRepository>()
            override val shareSchedulesRepository = instance<ShareSchedulesRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val messageRepository = instance<MessageRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val baseSchedulesRepository = instance<BaseScheduleRepository>()
            override val sourceSyncFacade = instance<SourceSyncFacade>()
            override val friendRequestsRepository = instance<FriendRequestsRepository>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val startClassesReminderManager = instance<StartClassesReminderManager>()
            override val endClassesReminderManager = instance<EndClassesReminderManager>()
            override val homeworksReminderManager = instance<HomeworksReminderManager>()
            override val workloadWarningManager = instance<WorkloadWarningManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val connectionManager = instance<Konnection>()
            override val dateManager = instance<DateManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<ProfileFeatureStarter> {
        with(ProfileFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<UsersFeatureDependencies> {
        object : UsersFeatureDependencies {
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val friendRequestsRepository = instance<FriendRequestsRepository>()
            override val shareSchedulesRepository = instance<ShareSchedulesRepository>()
            override val shareHomeworksRepository = instance<ShareHomeworksRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val messageRepository = instance<MessageRepository>()
            override val dateManager = instance<DateManager>()
            override val connectionManager = instance<Konnection>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<UsersFeatureStarter> {
        with(UsersFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<EditorFeatureDependencies> {
        object : EditorFeatureDependencies {
            override val billingFeatureStarter = provider<BillingFeatureStarter>()
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val homeworksRepository = instance<HomeworksRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val startClassesReminderManager = instance<StartClassesReminderManager>()
            override val endClassesReminderManager = instance<EndClassesReminderManager>()
            override val todoReminderManager = instance<TodoReminderManager>()
            override val usersRepository = instance<UsersRepository>()
            override val manageUserRepository = instance<ManageUserRepository>()
            override val dateManager = instance<DateManager>()
            override val overlayManager = instance<TimeOverlayManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<EditorFeatureStarter> {
        with(EditorFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<SettingsFeatureDependencies> {
        object : SettingsFeatureDependencies {
            override val billingFeatureStarter = provider<BillingFeatureStarter>()
            override val productsRepository = instance<ProductsRepository>()
            override val generalSettingsRepository = instance<GeneralSettingsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val homeworksRepository = instance<HomeworksRepository>()
            override val todosRepository = instance<TodoRepository>()
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val startClassesReminderManager = instance<StartClassesReminderManager>()
            override val endClassesReminderManager = instance<EndClassesReminderManager>()
            override val homeworksReminderManager = instance<HomeworksReminderManager>()
            override val workloadWarningManager = instance<WorkloadWarningManager>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val crashlyticsService = instance<CrashlyticsService>()
            override val iapService = instance<IapService>()
        }
    }

    bindProvider<SettingsFeatureStarter> {
        with(SettingsFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<BillingFeatureDependencies> {
        object : BillingFeatureDependencies {
            override val usersRepository = instance<UsersRepository>()
            override val productsRepository = instance<ProductsRepository>()
            override val manageUserRepository = instance<ManageUserRepository>()
            override val dateManager = instance<DateManager>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val coroutineManager = instance<CoroutineManager>()
            override val connectionManager = instance<Konnection>()
            override val iapService = instance<IapService>()
            override val crashlyticsService = instance<CrashlyticsService>()
            override val analyticsService = instance<AnalyticsService>()
        }
    }

    bindProvider<BillingFeatureStarter> {
        with(BillingFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<ChatFeatureDependencies> {
        object : ChatFeatureDependencies {
            override val billingFeatureStarter = provider<BillingFeatureStarter>()
            override val aiAssistantRepository = instance<AiAssistantRepository>()
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val homeworksRepository = instance<HomeworksRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val startClassesReminderManager = instance<StartClassesReminderManager>()
            override val endClassesReminderManager = instance<EndClassesReminderManager>()
            override val todoReminderManager = instance<TodoReminderManager>()
            override val usersRepository = instance<UsersRepository>()
            override val manageUserRepository = instance<ManageUserRepository>()
            override val dateManager = instance<DateManager>()
            override val overlayManager = instance<TimeOverlayManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bindProvider<ChatFeatureStarter> {
        with(ChatFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }
}