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

package navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorContent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import inject.FeatureScreen
import ru.aleshin.core.common.navigation.screens.EmptyScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
@Composable
fun <S : FeatureScreen> NestedFeatureNavigator(
    screenProvider: FeatureScreenProvider<S>,
    navigatorManager: NavigatorManager<S>,
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() },
) {
    val startScreen = navigatorManager.fetchStartScreen()
    Navigator(
        screens = listOf(screenProvider.provideFeatureScreen(startScreen)),
        onBackPressed = onBackPressed,
        disposeBehavior = NavigatorDisposeBehavior(
            disposeNestedNavigators = false,
            disposeSteps = true,
        ),
    ) { navigator ->
        DisposableEffect(Unit) {
            navigatorManager.attachNavigator(navigator)
            onDispose { navigatorManager.detachNavigator() }
        }
        content.invoke(navigator)
    }
}