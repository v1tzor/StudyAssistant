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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationState

/**
 * @author Stanislav Aleshin on 26.08.2025
 */
internal abstract class NotificationComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {

    abstract val store: NotificationComposeStore

    class Default(
        storeFactory: NotificationComposeStore.Factory,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<NotificationOutput>,
    ) : NotificationComponent(componentContext) {

        private companion object Companion {
            const val COMPONENT_KEY = "SETTINGS_NOTIFICATION"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = NotificationState(),
            stateSerializer = NotificationState.serializer(),
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )
    }
}