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

import managers.CoroutineManager
import managers.DateManager
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindProvider
import org.kodein.di.instance
import org.kodein.di.provider
import repositories.AuthRepository
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.ManageUserRepository
import repositories.OrganizationsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.impl.di.AuthFeatureDependencies
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.impl.di.EditorFeatureDependencies
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
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

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
val featureModule = DI.Module("Feature") {
    bindEagerSingleton<NavigationFeatureDependencies> {
        object : NavigationFeatureDependencies {
            override val profileFeatureStarter = provider<ProfileFeatureStarter>()
            override val scheduleFeatureStarter = provider<ScheduleFeatureStarter>()
            override val coroutineManager = instance<CoroutineManager>()
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
            override val editorFeatureStarter = provider<EditorFeatureStarter>()
            override val navigationFeatureStarter = provider<NavigationFeatureStarter>()
            override val authFeatureStarter = provider<AuthFeatureStarter>()
            override val usersRepository = instance<UsersRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val coroutineManager = instance<CoroutineManager>()
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
            override val manageUserRepository = instance<ManageUserRepository>()
            override val coroutineManager = instance<CoroutineManager>()
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
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<ScheduleFeatureStarter> {
        with(ScheduleFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<ProfileFeatureDependencies> {
        object : ProfileFeatureDependencies {
            override val authFeatureStarter = provider<AuthFeatureStarter>()
            override val authRepository = instance<AuthRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<ProfileFeatureStarter> {
        with(ProfileFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<EditorFeatureDependencies> {
        object : EditorFeatureDependencies {
            override val baseScheduleRepository = instance<BaseScheduleRepository>()
            override val customScheduleRepository = instance<CustomScheduleRepository>()
            override val organizationsRepository = instance<OrganizationsRepository>()
            override val calendarSettingsRepository = instance<CalendarSettingsRepository>()
            override val usersRepository = instance<UsersRepository>()
            override val dateManager = instance<DateManager>()
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<EditorFeatureStarter> {
        with(EditorFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }
}
