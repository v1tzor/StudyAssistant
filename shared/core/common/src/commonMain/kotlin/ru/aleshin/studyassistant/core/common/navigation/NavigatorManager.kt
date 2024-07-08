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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.FeatureScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
interface NavigatorManager<S : FeatureScreen> : NavigatorHolder, StartScreenHolder<S> {

    fun executeNavigation(command: NavigationCommand)

    abstract class Abstract<S : FeatureScreen>(
        private val commandBuffer: CommandBuffer
    ) : NavigatorManager<S>, StartScreenHolder.Abstract<S>() {

        private var navigator: Navigator? = null

        override fun executeNavigation(command: NavigationCommand) {
            commandBuffer.sendCommand(command)
        }

        override fun attachNavigator(navigator: Navigator) {
            this.navigator = navigator

            commandBuffer.setListener { command ->
                command.invoke(checkNotNull(this.navigator))
            }
        }

        override fun detachNavigator() {
            commandBuffer.removeListener()
            navigator = null
        }
    }
}

interface NavigatorHolder {
    fun attachNavigator(navigator: Navigator)
    fun detachNavigator()
}

interface StartScreenHolder<S : FeatureScreen> {

    fun fetchStartScreen(): S

    fun setStartScreen(screen: S): Boolean

    abstract class Abstract<S : FeatureScreen> : StartScreenHolder<S> {

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

@Composable
inline fun <reified T : NavigatorManager<S>, S : FeatureScreen> Screen.rememberNavigatorManager(): T {
    val di = localDI().direct
    return remember { di.instance<T>() }
}