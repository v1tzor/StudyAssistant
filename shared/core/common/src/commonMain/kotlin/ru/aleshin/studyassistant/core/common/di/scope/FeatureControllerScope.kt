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

package ru.aleshin.studyassistant.core.common.di.scope

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import org.kodein.di.bindings.Scope
import org.kodein.di.bindings.ScopeRegistry
import ru.aleshin.avtoshina.core.common.di.scope.RetainedScopeRegistry

/**
 * @author Stanislav Aleshin on 30.01.2026.
 */
object FeatureControllerScope : Scope<ComponentContext> {

    override fun getRegistry(context: ComponentContext): ScopeRegistry {
        return context.instanceKeeper.getOrCreate { RetainedScopeRegistry() }.delegate
    }
}