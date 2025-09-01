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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsInput
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsState

/**
 * @author Stanislav Aleshin on 25.08.2025
 */
internal abstract class SubjectsComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {

    abstract val store: SubjectsComposeStore

    class Default(
        storeFactory: SubjectsComposeStore.Factory,
        componentContext: ComponentContext,
        inputData: SubjectsInput,
        outputConsumer: OutputConsumer<SubjectsOutput>,
    ) : SubjectsComponent(componentContext) {

        private companion object Companion {
            const val COMPONENT_KEY = "INFO_SUBJECTS"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = SubjectsState(),
            stateSerializer = SubjectsState.serializer(),
            input = inputData,
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )
    }
}