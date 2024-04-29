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

package ru.aleshin.studyassistant.preview.impl.di

import inject.BaseFeatureDependencies
import managers.CoroutineManager
import repositories.AuthRepository
import repositories.CalendarSettingsRepository
import repositories.OrganizationsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
interface PreviewFeatureDependencies : BaseFeatureDependencies {
    val navigationFeatureStarter: () -> NavigationFeatureStarter
    val authFeatureStarter: () -> AuthFeatureStarter
    val usersRepository: UsersRepository
    val organizationsRepository: OrganizationsRepository
    val calendarSettingsRepository: CalendarSettingsRepository
    val coroutineManager: CoroutineManager
}