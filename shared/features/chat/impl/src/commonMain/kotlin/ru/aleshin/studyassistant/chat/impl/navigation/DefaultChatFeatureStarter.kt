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

package ru.aleshin.studyassistant.chat.impl.navigation

import ru.aleshin.studyassistant.chat.api.ChatFeatureApi
import ru.aleshin.studyassistant.chat.api.ChatFeatureStarter
import ru.aleshin.studyassistant.chat.impl.di.ChatFeatureDependencies
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureManager

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
public class DefaultChatFeatureStarter(
    private val dependenciesFactory: () -> ChatFeatureDependencies,
) : ChatFeatureStarter {

    override fun createOrGetFeature(): ChatFeatureApi {
        return ChatFeatureManager.createOrGetFeature(dependenciesFactory())
    }
}