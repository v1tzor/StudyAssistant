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

package ru.aleshin.studyassistant.preview.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewErrorHandler
import ru.aleshin.studyassistant.preview.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.OrganizationsInteractor

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<PreviewErrorHandler> { PreviewErrorHandler.Base() }
    bindSingleton<PreviewEitherWrapper> { PreviewEitherWrapper.Base(instance(), instance()) }

    bindProvider<AppUserInteractor> { AppUserInteractor.Base(instance(), instance()) }
    bindProvider<OrganizationsInteractor> { OrganizationsInteractor.Base(instance(), instance(), instance()) }
    bindProvider<GeneralSettingsInteractor> { GeneralSettingsInteractor.Base(instance(), instance()) }
    bindProvider<CalendarSettingsInteractor> { CalendarSettingsInteractor.Base(instance(), instance(), instance()) }
}