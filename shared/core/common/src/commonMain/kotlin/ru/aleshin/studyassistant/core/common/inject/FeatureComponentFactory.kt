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

package ru.aleshin.studyassistant.core.common.inject

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
interface FeatureComponentFactory<FC : FeatureComponent<C, O>, C, O : BaseOutput> {

    fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<C>,
        outputConsumer: OutputConsumer<O>
    ): FC
}