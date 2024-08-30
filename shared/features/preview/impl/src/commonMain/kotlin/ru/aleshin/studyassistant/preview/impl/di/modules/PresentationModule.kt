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
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewFeatureStarterImpl
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewScreenProvider
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.IntroEffectCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.IntroScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.IntroStateCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.navigation.NavScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupEffectCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupStateCommunicator
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.SetupWorkProcessor

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavScreenModel> { NavScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<PreviewFeatureStarter> { PreviewFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<PreviewScreenProvider> { PreviewScreenProvider.Base(instance<() -> AuthFeatureStarter>(), instance<() -> EditorFeatureStarter>(), instance<() -> NavigationFeatureStarter>()) }

    bindProvider<IntroStateCommunicator> { IntroStateCommunicator.Base() }
    bindProvider<IntroEffectCommunicator> { IntroEffectCommunicator.Base() }
    bindProvider<IntroScreenModel> { IntroScreenModel(instance(), instance(), instance(), instance()) }

    bindProvider<SetupStateCommunicator> { SetupStateCommunicator.Base() }
    bindProvider<SetupEffectCommunicator> { SetupEffectCommunicator.Base() }
    bindProvider<SetupWorkProcessor> { SetupWorkProcessor.Base(instance(), instance(), instance()) }
    bindProvider<SetupScreenModel> { SetupScreenModel(instance(), instance(), instance(), instance(), instance()) }
}