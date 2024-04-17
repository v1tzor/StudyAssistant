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
import org.kodein.di.bindFactory
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.factory
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen
import ru.aleshin.studyassistant.preview.impl.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewFeatureStarterImpl
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.IntroEffectCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.IntroScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.IntroStateCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.nav.NavScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.nav.NavigationScreen
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupEffectCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupStateCommunicator

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavScreenModel> { NavScreenModel() }
    bindFactory<PreviewScreen, NavigationScreen> { initScreen: PreviewScreen -> NavigationScreen(initScreen) }

    bindProvider<PreviewFeatureStarter> { PreviewFeatureStarterImpl(factory()) }
    bindProvider<FeatureScreenProvider> { FeatureScreenProvider.Base(instance<() -> AuthFeatureStarter>()) }

    bindProvider<IntroStateCommunicator> { IntroStateCommunicator.Base() }
    bindProvider<IntroEffectCommunicator> { IntroEffectCommunicator.Base() }
    bindProvider<IntroScreenModel> { IntroScreenModel(instance(), instance(), instance(), instance()) }

    bindProvider<SetupStateCommunicator> { SetupStateCommunicator.Base() }
    bindProvider<SetupEffectCommunicator> { SetupEffectCommunicator.Base() }
    bindProvider<SetupScreenModel> { SetupScreenModel(instance(), instance(), instance()) }
}