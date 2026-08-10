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

package ru.aleshin.studyassistant.users.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponentDeps
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.users.api.UsersConfig
import ru.aleshin.studyassistant.users.api.UsersContentProviderFactory
import ru.aleshin.studyassistant.users.api.UsersOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.root.UsersContentProvider
import ru.aleshin.studyassistant.users.impl.presentation.ui.root.UsersFeatureComponent

internal class DefaultUsersContentProviderFactory(
    private val di: DI,
) : UsersContentProviderFactory {
    override fun createProvider(
        componentContext: ComponentContext,
        startConfig: UsersConfig,
        outputConsumer: OutputConsumer<UsersOutput>,
    ): FeatureContentProvider {
        val deps = UsersComponentDeps(startConfig, outputConsumer)
        val component by di.on(componentContext).instance<UsersComponentDeps, UsersFeatureComponent>(arg = deps)
        return UsersContentProvider(di, component)
    }
}

internal typealias UsersComponentDeps = FeatureComponentDeps<UsersConfig, UsersOutput>
