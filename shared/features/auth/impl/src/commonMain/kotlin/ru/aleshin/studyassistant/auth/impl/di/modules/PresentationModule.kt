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
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.impl.navigation.AuthFeatureStarterImpl
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.screenmodel.ForgotEffectCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.screenmodel.ForgotScreenModel
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.screenmodel.ForgotStateCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.screenmodel.ForgotWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.screenmodel.LoginEffectCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.screenmodel.LoginScreenModel
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.screenmodel.LoginStateCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.screenmodel.LoginWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.ui.navigation.NavScreenModel
import ru.aleshin.studyassistant.auth.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel.RegisterEffectCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel.RegisterScreenModel
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel.RegisterStateCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel.RegisterWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.screenmodel.VerificationEffectCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.screenmodel.VerificationScreenModel
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.screenmodel.VerificationStateCommunicator
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.screenmodel.VerificationWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.PasswordValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.UsernameValidator
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavScreenModel> { NavScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<AuthFeatureStarter> { AuthFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<AuthScreenProvider> { AuthScreenProvider.Base(instance<() -> PreviewFeatureStarter>(), instance<() -> NavigationFeatureStarter>()) }

    bindSingleton<EmailValidator> { EmailValidator.Base() }
    bindSingleton<PasswordValidator> { PasswordValidator.Base() }
    bindSingleton<UsernameValidator> { UsernameValidator.Base() }

    bindProvider<LoginStateCommunicator> { LoginStateCommunicator.Base() }
    bindProvider<LoginEffectCommunicator> { LoginEffectCommunicator.Base() }
    bindProvider<LoginWorkProcessor> { LoginWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindProvider<LoginScreenModel> { LoginScreenModel(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<RegisterStateCommunicator> { RegisterStateCommunicator.Base() }
    bindProvider<RegisterEffectCommunicator> { RegisterEffectCommunicator.Base() }
    bindProvider<RegisterWorkProcessor> { RegisterWorkProcessor.Base(instance(), instance(), instance()) }
    bindProvider<RegisterScreenModel> { RegisterScreenModel(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<ForgotStateCommunicator> { ForgotStateCommunicator.Base() }
    bindProvider<ForgotEffectCommunicator> { ForgotEffectCommunicator.Base() }
    bindProvider<ForgotWorkProcessor> { ForgotWorkProcessor.Base(instance(), instance()) }
    bindProvider<ForgotScreenModel> { ForgotScreenModel(instance(), instance(), instance(), instance(), instance(), instance()) }

    bindProvider<VerificationStateCommunicator> { VerificationStateCommunicator.Base() }
    bindProvider<VerificationEffectCommunicator> { VerificationEffectCommunicator.Base() }
    bindProvider<VerificationWorkProcessor> { VerificationWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindProvider<VerificationScreenModel> { VerificationScreenModel(instance(), instance(), instance(), instance()) }
}