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

package ru.aleshin.studyassistant.users.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.users.impl.domain.common.UsersEitherWrapper
import ru.aleshin.studyassistant.users.impl.domain.common.UsersErrorHandler
import ru.aleshin.studyassistant.users.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.users.impl.domain.interactors.FriendRequestsInteractor
import ru.aleshin.studyassistant.users.impl.domain.interactors.UsersInteractor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<UsersErrorHandler> { UsersErrorHandler.Base() }
    bindSingleton<UsersEitherWrapper> { UsersEitherWrapper.Base(instance()) }

    bindSingleton<EmployeeInteractor> { EmployeeInteractor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<FriendRequestsInteractor> { FriendRequestsInteractor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<UsersInteractor> { UsersInteractor.Base(instance(), instance()) }
}