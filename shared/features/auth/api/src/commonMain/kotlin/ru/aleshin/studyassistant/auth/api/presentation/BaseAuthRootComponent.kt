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

package ru.aleshin.studyassistant.auth.api.presentation

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.RootComponent

/**
 * @author Stanislav Aleshin on 20.08.2025.
 */
abstract class BaseAuthRootComponent(
    componentContext: ComponentContext
) : RootComponent(componentContext) {

    @Serializable
    sealed class Config {

        @Serializable
        data object Login : Config()

        @Serializable
        data object Register : Config()

        @Serializable
        data object Forgot : Config()

        @Serializable
        data object Verification : Config()
    }

    sealed class Output : BaseOutput {
        data object NavigateToBack : Output()
        data object NavigateToFirstSetup : Output()
        data object NavigateToApp : Output()
    }
}