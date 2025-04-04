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

package ru.aleshin.studyassistant.core.common.navigation

import ru.aleshin.studyassistant.core.common.inject.FeatureScreen
import ru.aleshin.studyassistant.core.common.inject.RootScreen

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
interface StartScreenHolder<S : FeatureScreen<R>, R : RootScreen> {

    fun fetchStartScreen(): S

    fun setStartScreen(screen: S): Boolean

    abstract class Abstract<S : FeatureScreen<R>, R : RootScreen> : StartScreenHolder<S, R> {

        private var screen: S? = null

        override fun fetchStartScreen(): S {
            return checkNotNull(screen)
        }

        override fun setStartScreen(screen: S): Boolean {
            val isSet = this.screen == null
            this.screen = screen
            return isSet
        }
    }
}