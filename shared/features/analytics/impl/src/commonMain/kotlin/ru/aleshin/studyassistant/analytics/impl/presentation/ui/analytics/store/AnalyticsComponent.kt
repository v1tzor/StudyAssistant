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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsInput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsOutput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsState
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal abstract class AnalyticsComponent(
    componentContext: ComponentContext,
) : ChildComponent(componentContext) {

    abstract val store: AnalyticsComposeStore

    class Default(
        storeFactory: AnalyticsComposeStore.Factory,
        componentContext: ComponentContext,
        inputData: AnalyticsInput,
        outputConsumer: OutputConsumer<AnalyticsOutput>,
    ) : AnalyticsComponent(componentContext) {

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = AnalyticsState(),
            stateSerializer = AnalyticsState.serializer(),
            input = inputData,
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )

        private companion object {
            const val COMPONENT_KEY = "ANALYTICS_SCREEN"
        }
    }
}
