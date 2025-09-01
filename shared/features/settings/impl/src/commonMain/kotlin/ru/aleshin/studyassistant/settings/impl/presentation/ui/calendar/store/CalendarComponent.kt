/*
 * Copyright 2025 Stanislav Aleshin
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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarState

/**
 * @author Stanislav Aleshin on 26.08.2025
 */
internal abstract class CalendarComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {

    abstract val store: CalendarComposeStore

    class Default(
        storeFactory: CalendarComposeStore.Factory,
        componentContext: ComponentContext,
    ) : CalendarComponent(componentContext) {

        private companion object Companion {
            const val COMPONENT_KEY = "EDITOR_CALENDAR"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = CalendarState(),
            stateSerializer = CalendarState.serializer(),
            storeKey = COMPONENT_KEY,
        )
    }
}