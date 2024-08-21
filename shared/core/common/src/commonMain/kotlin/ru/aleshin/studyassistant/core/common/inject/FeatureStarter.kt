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

package ru.aleshin.studyassistant.core.common.inject

import ru.aleshin.studyassistant.core.common.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.core.common.navigation.NavigatorManager

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
interface FeatureStarter {

    interface WithNestedNavigation<S : FeatureScreen<R>, R : RootScreen> {

        fun fetchRootScreenAndNavigate(screen: S, isNavigate: Boolean = true): R

        abstract class Abstract<S : FeatureScreen<R>, R : RootScreen>(
            private val featureNavScreen: R,
            private val navigatorManager: NavigatorManager<S, R>,
            private val screenProvider: FeatureScreenProvider<S, R>,
        ) : WithNestedNavigation<S, R> {

            override fun fetchRootScreenAndNavigate(screen: S, isNavigate: Boolean): R {
                val featureScreen = screenProvider.provideFeatureScreen(screen)
                val isSetStartScreen = navigatorManager.setStartScreen(screen)
                if (!isSetStartScreen && isNavigate) {
                    navigatorManager.executeNavigation {
                        val lastScreen = lastItemOrNull
                        if (lastScreen?.key != featureScreen.key) push(featureScreen)
                    }
                }
                return featureNavScreen
            }
        }
    }

    interface WithSingleNavigation<R : RootScreen> {

        fun fetchFeatureScreen(): R

        abstract class Abstract<R : RootScreen>(private val mainScreen: R) : WithSingleNavigation<R> {

            override fun fetchFeatureScreen(): R {
                return mainScreen
            }
        }
    }
}