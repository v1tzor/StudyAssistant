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

package ru.aleshin.studyassistant.profile.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileEitherWrapper
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileErrorHandler
import ru.aleshin.studyassistant.profile.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.FriendRequestsInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.ReminderInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.ShareSchedulesInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.UserInteractor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<ProfileErrorHandler> { ProfileErrorHandler.Base() }
    bindSingleton<ProfileEitherWrapper> { ProfileEitherWrapper.Base(instance()) }

    bindProvider<AuthInteractor> { AuthInteractor.Base(instance(), instance(), instance(), instance()) }
    bindProvider<UserInteractor> { UserInteractor.Base(instance(), instance()) }
    bindProvider<FriendRequestsInteractor> { FriendRequestsInteractor.Base(instance(), instance(), instance()) }
    bindProvider<OrganizationsInteractor> { OrganizationsInteractor.Base(instance(), instance(), instance()) }
    bindProvider<ShareSchedulesInteractor> { ShareSchedulesInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindProvider<ReminderInteractor> { ReminderInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
}