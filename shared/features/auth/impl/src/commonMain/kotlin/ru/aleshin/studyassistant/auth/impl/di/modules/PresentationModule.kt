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
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponentFactory
import ru.aleshin.studyassistant.auth.impl.navigation.DefaultAuthComponentFactory
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store.ForgotComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store.ForgotWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store.LoginComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store.LoginWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store.RegisterComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store.RegisterWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store.VerificationComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store.VerificationWorkProcessor
import ru.aleshin.studyassistant.auth.impl.presentation.validation.EmailValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.PasswordValidator
import ru.aleshin.studyassistant.auth.impl.presentation.validation.UsernameValidator

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<AuthFeatureComponentFactory> { DefaultAuthComponentFactory(instance(), instance(), instance(), instance()) }

    bindSingleton<EmailValidator> { EmailValidator.Base() }
    bindSingleton<PasswordValidator> { PasswordValidator.Base() }
    bindSingleton<UsernameValidator> { UsernameValidator.Base() }

    bindSingleton<LoginWorkProcessor> { LoginWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<LoginComposeStore.Factory> { LoginComposeStore.Factory(instance(), instance(), instance(), instance()) }

    bindSingleton<RegisterWorkProcessor> { RegisterWorkProcessor.Base(instance(), instance()) }
    bindSingleton<RegisterComposeStore.Factory> { RegisterComposeStore.Factory(instance(), instance(), instance(), instance(), instance()) }

    bindSingleton<ForgotWorkProcessor> { ForgotWorkProcessor.Base(instance()) }
    bindSingleton<ForgotComposeStore.Factory> { ForgotComposeStore.Factory(instance(), instance(), instance()) }

    bindSingleton<VerificationWorkProcessor> { VerificationWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<VerificationComposeStore.Factory> { VerificationComposeStore.Factory(instance(), instance()) }
}