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
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.provider
import ru.aleshin.studyassistant.navigation.GlobalScreenProvider
import ru.aleshin.studyassistant.presentation.ui.main.screenmodel.MainEffectCommunicator
import ru.aleshin.studyassistant.presentation.ui.main.screenmodel.MainScreenModel
import ru.aleshin.studyassistant.presentation.ui.main.screenmodel.MainStateCommunicator
import ru.aleshin.studyassistant.presentation.ui.main.screenmodel.MainWorkProcessor

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
val presentationModule = DI.Module("Presentation") {
    bindProvider<GlobalScreenProvider> { GlobalScreenProvider.Base(provider(), provider(), provider(), provider(), provider()) }

    bindSingleton<MainStateCommunicator> { MainStateCommunicator.Base() }
    bindSingleton<MainEffectCommunicator> { MainEffectCommunicator.Base() }
    bindSingleton<MainWorkProcessor> { MainWorkProcessor.Base(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<MainScreenModel> { MainScreenModel(instance(), instance(), instance(), instance()) }
}