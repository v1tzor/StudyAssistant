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

package ru.aleshin.studyassistant.billing.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.impl.domain.common.BillingEitherWrapper
import ru.aleshin.studyassistant.billing.impl.domain.common.BillingErrorHandler
import ru.aleshin.studyassistant.billing.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.billing.impl.domain.interactors.PurchaseInteractor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<BillingErrorHandler> { BillingErrorHandler.Base() }
    bindSingleton<BillingEitherWrapper> { BillingEitherWrapper.Base(instance(), instance()) }

    bindSingleton<PurchaseInteractor> { PurchaseInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<AppUserInteractor> { AppUserInteractor.Base(instance(), instance()) }
}