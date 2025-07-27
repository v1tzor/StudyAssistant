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

package ru.aleshin.studyassistant.auth.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthEitherWrapper
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthErrorHandler
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<AuthErrorHandler> { AuthErrorHandler.Base() }
    bindSingleton<AuthEitherWrapper> { AuthEitherWrapper.Base(instance(), instance()) }
    bindSingleton<AuthInteractor> { AuthInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<AppUserInteractor> { AppUserInteractor.Base(instance(), instance()) }
}