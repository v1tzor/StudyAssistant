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

package ru.aleshin.studyassistant.tasks.impl.navigation

import ru.aleshin.studyassistant.tasks.api.TasksFeatureApi
import ru.aleshin.studyassistant.tasks.api.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.impl.di.TasksFeatureDependencies
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureManager

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
public class DefaultTasksFeatureStarter(
    private val dependenciesFactory: () -> TasksFeatureDependencies,
) : TasksFeatureStarter {

    override fun createOrGetFeature(): TasksFeatureApi {
        return TasksFeatureManager.createOrGetFeature(dependenciesFactory())
    }
}