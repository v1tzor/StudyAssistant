/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsState

/**
 * @author Stanislav Aleshin on 05.07.2026.
 */
internal abstract class AiSettingsComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {
    abstract val store: AiSettingsComposeStore

    class Default(
        storeFactory: AiSettingsComposeStore.Factory,
        componentContext: ComponentContext,
    ) : AiSettingsComponent(componentContext) {
        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = AiSettingsState(),
            stateSerializer = AiSettingsState.serializer(),
            storeKey = "SETTINGS_AI",
        )
    }
}
