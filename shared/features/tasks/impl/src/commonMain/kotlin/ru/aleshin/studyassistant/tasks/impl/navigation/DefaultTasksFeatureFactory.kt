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

package ru.aleshin.studyassistant.tasks.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.tasks.api.TasksDecomposeFeatureFactory
import ru.aleshin.studyassistant.tasks.api.TasksFeatureApi
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureController

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
public class DefaultTasksFeatureFactory(
    private val di: DI,
) : TasksDecomposeFeatureFactory {

    override fun createOrGetFeature(context: ComponentContext): TasksFeatureApi {
        return di.direct.on(context).instance<TasksFeatureController>().fetchApi()
    }
}
