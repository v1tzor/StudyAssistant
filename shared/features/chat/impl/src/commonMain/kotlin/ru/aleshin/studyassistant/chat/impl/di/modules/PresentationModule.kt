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

package ru.aleshin.studyassistant.chat.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.chat.api.navigation.ChatFeatureStarter
import ru.aleshin.studyassistant.chat.impl.navigation.ChatFeatureStarterImpl
import ru.aleshin.studyassistant.chat.impl.navigation.ChatScreenProvider
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.screenmodel.AssistantEffectCommunicator
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.screenmodel.AssistantScreenModel
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.screenmodel.AssistantStateCommunicator
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.screenmodel.AssistantWorkProcessor
import ru.aleshin.studyassistant.chat.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.chat.impl.presentation.ui.navigation.NavigationScreenModel

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<ChatFeatureStarter> { ChatFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<ChatScreenProvider> { ChatScreenProvider.Base(instance()) }

    bindProvider<AssistantStateCommunicator> { AssistantStateCommunicator.Base() }
    bindProvider<AssistantEffectCommunicator> { AssistantEffectCommunicator.Base() }
    bindProvider<AssistantWorkProcessor> { AssistantWorkProcessor.Base(instance()) }
    bindProvider<AssistantScreenModel> { AssistantScreenModel(instance(), instance(), instance(), instance(), instance()) }
}