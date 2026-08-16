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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal abstract class ImportComponent(
    componentContext: ComponentContext,
) : ChildComponent(componentContext) {

    abstract val store: ImportComposeStore

    class Default(
        storeFactory: ImportComposeStore.Factory,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<ImportOutput>,
        input: ImportInput = ImportInput,
    ) : ImportComponent(componentContext) {

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = ImportState(),
            stateSerializer = ImportState.serializer(),
            input = input,
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )

        private companion object {
            const val COMPONENT_KEY = "SCHEDULE_IMPORT"
        }
    }
}
