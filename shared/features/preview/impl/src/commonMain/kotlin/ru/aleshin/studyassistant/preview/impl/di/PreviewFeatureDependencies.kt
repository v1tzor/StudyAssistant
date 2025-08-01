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

import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
public interface PreviewFeatureDependencies : BaseFeatureDependencies {
    public val editorFeatureStarter: () -> EditorFeatureStarter
    public val navigationFeatureStarter: () -> NavigationFeatureStarter
    public val authFeatureStarter: () -> AuthFeatureStarter
    public val billingFeatureStarter: () -> BillingFeatureStarter
    public val usersRepository: UsersRepository
    public val organizationsRepository: OrganizationsRepository
    public val generalSettingsRepository: GeneralSettingsRepository
    public val calendarSettingsRepository: CalendarSettingsRepository
    public val deviceInfoProvider: DeviceInfoProvider
    public val coroutineManager: CoroutineManager
    public val crashlyticsService: CrashlyticsService
}