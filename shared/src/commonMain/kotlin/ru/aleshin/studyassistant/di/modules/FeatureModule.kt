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

package ru.aleshin.studyassistant.di.modules

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import ru.aleshin.studyassistant.analytics.api.AnalyticsDecomposeFeatureFactory
import ru.aleshin.studyassistant.analytics.impl.di.AnalyticsFeatureDependencies
import ru.aleshin.studyassistant.analytics.impl.di.holder.AnalyticsFeatureController
import ru.aleshin.studyassistant.analytics.impl.navigation.DefaultAnalyticsFeatureFactory
import ru.aleshin.studyassistant.chat.api.ChatDecomposeFeatureFactory
import ru.aleshin.studyassistant.chat.impl.di.ChatFeatureDependencies
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureController
import ru.aleshin.studyassistant.chat.impl.navigation.DefaultChatFeatureFactory
import ru.aleshin.studyassistant.core.common.di.scope.FeatureControllerScope
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.managers.TimeOverlayManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworkShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.editor.api.EditorDecomposeFeatureFactory
import ru.aleshin.studyassistant.editor.impl.di.EditorFeatureDependencies
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureController
import ru.aleshin.studyassistant.editor.impl.navigation.DefaultEditorFeatureFactory
import ru.aleshin.studyassistant.info.api.InfoDecomposeFeatureFactory
import ru.aleshin.studyassistant.info.impl.di.InfoFeatureDependencies
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureController
import ru.aleshin.studyassistant.info.impl.navigation.DefaultInfoFeatureFactory
import ru.aleshin.studyassistant.preview.api.PreviewDecomposeFeatureFactory
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureController
import ru.aleshin.studyassistant.preview.impl.navigation.DefaultPreviewFeatureFactory
import ru.aleshin.studyassistant.profile.api.ProfileDecomposeFeatureFactory
import ru.aleshin.studyassistant.profile.impl.di.ProfileFeatureDependencies
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureController
import ru.aleshin.studyassistant.profile.impl.navigation.DefaultProfileFeatureFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleDecomposeFeatureFactory
import ru.aleshin.studyassistant.schedule.impl.di.ScheduleFeatureDependencies
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureController
import ru.aleshin.studyassistant.schedule.impl.navigation.DefaultScheduleFeatureFactory
import ru.aleshin.studyassistant.settings.api.SettingsDecomposeFeatureFactory
import ru.aleshin.studyassistant.settings.impl.di.SettingsFeatureDependencies
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureController
import ru.aleshin.studyassistant.settings.impl.navigation.DefaultSettingsFeatureFactory
import ru.aleshin.studyassistant.tasks.api.TasksDecomposeFeatureFactory
import ru.aleshin.studyassistant.tasks.impl.di.TasksFeatureDependencies
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureController
import ru.aleshin.studyassistant.tasks.impl.navigation.DefaultTasksFeatureFactory
import ru.aleshin.studyassistant.users.api.UsersDecomposeFeatureFactory
import ru.aleshin.studyassistant.users.impl.di.UsersFeatureDependencies
import ru.aleshin.studyassistant.users.impl.di.holder.UsersFeatureController
import ru.aleshin.studyassistant.users.impl.navigation.DefaultUsersFeatureFactory

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
val featureModule = DI.Module("Feature") {
    bindProvider<AnalyticsFeatureDependencies> {
        object : AnalyticsFeatureDependencies {
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val homeworksRepository = instance<HomeworksRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<AnalyticsFeatureController>() with scoped(FeatureControllerScope).singleton {
        AnalyticsFeatureController(dependencies = instance())
    }
    bindSingleton<AnalyticsDecomposeFeatureFactory> {
        DefaultAnalyticsFeatureFactory(di)
    }

    bindProvider<PreviewFeatureDependencies> {
        object : PreviewFeatureDependencies {
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val profileRepository = instance<ProfileRepository>()
            override val generalSettingsRepository = instance<GeneralSettingsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val coroutineManager = instance<CoroutineManager>()
            override val dateManager = instance<DateManager>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<PreviewFeatureController>() with scoped(FeatureControllerScope).singleton {
        PreviewFeatureController(dependencies = instance())
    }
    bindSingleton<PreviewDecomposeFeatureFactory> {
        DefaultPreviewFeatureFactory(di)
    }

    bindProvider<ScheduleFeatureDependencies> {
        object : ScheduleFeatureDependencies {
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val scheduleShareRepository = instance<ScheduleShareRepository>()
            override val scheduleImportRepository = instance<ScheduleImportRepository>()
            override val adRewardRepository = instance<AdRewardRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val profileRepository = instance<ProfileRepository>()
            override val homeworkRepository = instance<HomeworksRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val startClassesReminderManager = instance<StartClassesReminderManager>()
            override val endClassesReminderManager = instance<EndClassesReminderManager>()
            override val connectionManager = instance<Konnection>()
            override val deviceInfoProvider = instance<DeviceInfoProvider>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val appDispatchers = instance<AppDispatchers>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<ScheduleFeatureController>() with scoped(FeatureControllerScope).singleton {
        ScheduleFeatureController(dependencies = instance())
    }
    bindSingleton<ScheduleDecomposeFeatureFactory> {
        DefaultScheduleFeatureFactory(di)
    }

    bindProvider<TasksFeatureDependencies> {
        object : TasksFeatureDependencies {
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val profileRepository = instance<ProfileRepository>()
            override val homeworkRepository = instance<HomeworksRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val homeworkShareRepository = instance<HomeworkShareRepository>()
            override val todoRepository = instance<TodoRepository>()
            override val todoReminderManager = instance<TodoReminderManager>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val connectionManager = instance<Konnection>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<TasksFeatureController>() with scoped(FeatureControllerScope).singleton {
        TasksFeatureController(dependencies = instance())
    }
    bindSingleton<TasksDecomposeFeatureFactory> {
        DefaultTasksFeatureFactory(di)
    }

    bindProvider<InfoFeatureDependencies> {
        object : InfoFeatureDependencies {
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val employeeRepository = instance<EmployeeRepository>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<InfoFeatureController>() with scoped(FeatureControllerScope).singleton {
        InfoFeatureController(dependencies = instance())
    }
    bindSingleton<InfoDecomposeFeatureFactory> {
        DefaultInfoFeatureFactory(di)
    }

    bindProvider<ProfileFeatureDependencies> {
        object : ProfileFeatureDependencies {
            override val profileRepository = instance<ProfileRepository>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<ProfileFeatureController>() with scoped(FeatureControllerScope).singleton {
        ProfileFeatureController(dependencies = instance())
    }
    bindSingleton<ProfileDecomposeFeatureFactory> {
        DefaultProfileFeatureFactory(di)
    }

    bindProvider<UsersFeatureDependencies> {
        object : UsersFeatureDependencies {
            override val employeeRepository = instance<EmployeeRepository>()
            override val subjectsRepository = instance<SubjectsRepository>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<UsersFeatureController>() with scoped(FeatureControllerScope).singleton {
        UsersFeatureController(dependencies = instance())
    }
    bindSingleton<UsersDecomposeFeatureFactory> {
        DefaultUsersFeatureFactory(di)
    }

    bindProvider<EditorFeatureDependencies> {
        object : EditorFeatureDependencies {
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
            override val profileRepository = instance<ProfileRepository>()
            override val dateManager = instance<DateManager>()
            override val overlayManager = instance<TimeOverlayManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<EditorFeatureController>() with scoped(FeatureControllerScope).singleton {
        EditorFeatureController(dependencies = instance())
    }
    bindSingleton<EditorDecomposeFeatureFactory> {
        DefaultEditorFeatureFactory(di)
    }

    bindProvider<SettingsFeatureDependencies> {
        object : SettingsFeatureDependencies {
            override val aiAssistantRepository = instance<AiAssistantRepository>()
            override val aiSettingsRepository = instance<AiSettingsRepository>()
            override val generalSettingsRepository = instance<GeneralSettingsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val notificationSettingsRepository = instance<NotificationSettingsRepository>()
            override val goalsRepository = instance<DailyGoalsRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
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
        }
    }
    bind<SettingsFeatureController>() with scoped(FeatureControllerScope).singleton {
        SettingsFeatureController(dependencies = instance())
    }
    bindSingleton<SettingsDecomposeFeatureFactory> {
        DefaultSettingsFeatureFactory(di)
    }

    bindProvider<ChatFeatureDependencies> {
        object : ChatFeatureDependencies {
            override val aiAssistantRepository = instance<AiAssistantRepository>()
            override val aiSettingsRepository = instance<AiSettingsRepository>()
            override val adRewardRepository = instance<AdRewardRepository>()
            override val profileRepository = instance<ProfileRepository>()
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
            override val dateManager = instance<DateManager>()
            override val overlayManager = instance<TimeOverlayManager>()
            override val coroutineManager = instance<CoroutineManager>()
            override val crashlyticsService = instance<CrashlyticsService>()
        }
    }
    bind<ChatFeatureController>() with scoped(FeatureControllerScope).singleton {
        ChatFeatureController(dependencies = instance())
    }
    bindSingleton<ChatDecomposeFeatureFactory> {
        DefaultChatFeatureFactory(di)
    }
}
