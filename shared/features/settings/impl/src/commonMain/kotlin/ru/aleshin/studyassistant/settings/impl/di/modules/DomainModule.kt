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

package ru.aleshin.studyassistant.settings.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsErrorHandler
import ru.aleshin.studyassistant.settings.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.NotificationSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.SubscriptionInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.SyncInteractor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<SettingsErrorHandler> { SettingsErrorHandler.Base() }
    bindSingleton<SettingsEitherWrapper> { SettingsEitherWrapper.Base(instance(), instance()) }

    bindSingleton<GeneralSettingsInteractor> { GeneralSettingsInteractor.Base(instance(), instance()) }
    bindSingleton<SubscriptionInteractor> { SubscriptionInteractor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<CalendarSettingsInteractor> { CalendarSettingsInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<NotificationSettingsInteractor> { NotificationSettingsInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<OrganizationInteractor> { OrganizationInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<AppUserInteractor> { AppUserInteractor.Base(instance(), instance()) }
    bindSingleton<SyncInteractor> { SyncInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
}