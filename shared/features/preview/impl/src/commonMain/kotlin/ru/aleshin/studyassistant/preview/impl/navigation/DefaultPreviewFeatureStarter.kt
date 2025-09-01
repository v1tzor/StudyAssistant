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

package ru.aleshin.studyassistant.preview.impl.navigation

import ru.aleshin.studyassistant.preview.api.PreviewFeatureApi
import ru.aleshin.studyassistant.preview.api.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureManager

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
public class DefaultPreviewFeatureStarter(
    private val dependenciesFactory: () -> PreviewFeatureDependencies,
) : PreviewFeatureStarter {

    override fun createOrGetFeature(): PreviewFeatureApi {
        return PreviewFeatureManager.createOrGetFeature(dependenciesFactory())
    }
}