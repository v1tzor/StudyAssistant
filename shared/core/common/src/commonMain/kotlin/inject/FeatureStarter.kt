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

package inject

import cafe.adriel.voyager.core.screen.Screen
import navigation.FeatureScreenProvider
import navigation.NavigatorManager

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
interface FeatureStarter<S : FeatureScreen> {

    fun fetchFeatureScreen(screen: S): Screen

    abstract class Navigation<S : FeatureScreen>(
        private val featureNavScreen: Screen,
        private val navigatorManager: NavigatorManager<S>,
        private val screenProvider: FeatureScreenProvider<S>,
    ) : FeatureStarter<S> {

        override fun fetchFeatureScreen(screen: S): Screen {
            val isSetStartScreen = navigatorManager.setStartScreen(screen)
            if (!isSetStartScreen) {
                val featureScreen = screenProvider.provideFeatureScreen(screen)
                navigatorManager.executeNavigation { push(featureScreen) }
            }
            return featureNavScreen
        }
    }

    abstract class SingleScreen(
        private val mainScreen: Screen,
    ) : FeatureStarter<MainScreen> {

        override fun fetchFeatureScreen(screen: MainScreen): Screen {
            return mainScreen
        }
    }
}