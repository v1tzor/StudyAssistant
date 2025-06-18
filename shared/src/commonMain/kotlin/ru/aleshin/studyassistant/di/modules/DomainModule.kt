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

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.domain.common.MainEitherWrapper
import ru.aleshin.studyassistant.domain.common.MainErrorHandler
import ru.aleshin.studyassistant.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.domain.interactors.ReminderInteractor

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
val domainModule = DI.Module("DomainModule") {
    bindSingleton<MainErrorHandler> { MainErrorHandler.Base() }
    bindSingleton<MainEitherWrapper> { MainEitherWrapper.Base(instance(), instance()) }

    bindSingleton<AppUserInteractor> { AppUserInteractor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<GeneralSettingsInteractor> { GeneralSettingsInteractor.Base(instance(), instance()) }
    bindSingleton<ReminderInteractor> { ReminderInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
}