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

package ru.aleshin.studyassistant.chat.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.chat.api.ChatDecomposeFeatureFactory
import ru.aleshin.studyassistant.chat.api.ChatFeatureApi
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureController

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
public class DefaultChatFeatureFactory(
    private val di: DI,
) : ChatDecomposeFeatureFactory {

    override fun createOrGetFeature(context: ComponentContext): ChatFeatureApi {
        return di.direct.on(context).instance<ChatFeatureController>().fetchApi()
    }
}
