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
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileFeatureStarterImpl
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.ui.ProfileScreen
import ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel.ProfileEffectCommunicator
import ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel.ProfileScreenModel
import ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel.ProfileStateCommunicator
import ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel.ProfileWorkProcessor
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<ProfileScreen> { ProfileScreen() }

    bindProvider<ProfileFeatureStarter> { ProfileFeatureStarterImpl(instance()) }
    bindProvider<ProfileScreenProvider> { ProfileScreenProvider.Base(instance<() -> AuthFeatureStarter>(), instance<() -> UsersFeatureStarter>(), instance<() -> SettingsFeatureStarter>()) }

    bindSingleton<ProfileStateCommunicator> { ProfileStateCommunicator.Base() }
    bindSingleton<ProfileEffectCommunicator> { ProfileEffectCommunicator.Base() }
    bindSingleton<ProfileWorkProcessor> { ProfileWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<ProfileScreenModel> { ProfileScreenModel(instance(), instance(), instance(), instance(), instance()) }
}