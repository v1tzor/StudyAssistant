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

package ru.aleshin.studyassistant.profile.impl.navigation

import ru.aleshin.studyassistant.profile.api.ProfileFeatureApi
import ru.aleshin.studyassistant.profile.api.ProfileFeatureStarter
import ru.aleshin.studyassistant.profile.impl.di.ProfileFeatureDependencies
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureManager

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
public class DefaultProfileFeatureStarter(
    private val dependenciesFactory: () -> ProfileFeatureDependencies,
) : ProfileFeatureStarter {

    override fun createOrGetFeature(): ProfileFeatureApi {
        return ProfileFeatureManager.createOrGetFeature(dependenciesFactory())
    }
}