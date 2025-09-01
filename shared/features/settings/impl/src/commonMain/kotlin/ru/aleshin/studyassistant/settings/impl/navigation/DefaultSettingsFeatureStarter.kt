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

package ru.aleshin.studyassistant.settings.impl.navigation

import ru.aleshin.studyassistant.settings.api.SettingsFeatureApi
import ru.aleshin.studyassistant.settings.api.SettingsFeatureStarter
import ru.aleshin.studyassistant.settings.impl.di.SettingsFeatureDependencies
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureManager

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
public class DefaultSettingsFeatureStarter(
    private val dependenciesFactory: () -> SettingsFeatureDependencies,
) : SettingsFeatureStarter {

    override fun createOrGetFeature(): SettingsFeatureApi {
        return SettingsFeatureManager.createOrGetFeature(dependenciesFactory())
    }
}