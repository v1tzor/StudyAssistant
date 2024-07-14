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
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorContent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.OnBackPressed
import cafe.adriel.voyager.navigator.internal.BackHandler
import ru.aleshin.studyassistant.core.common.inject.FeatureScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
@Composable
@OptIn(InternalVoyagerApi::class)
fun <S : FeatureScreen> NestedFeatureNavigator(
    screenProvider: FeatureScreenProvider<S>,
    navigatorManager: NavigatorManager<S>,
    onBackPressed: OnBackPressed = { true },
    content: NavigatorContent = { CurrentScreen() },
) {
    val startScreen = navigatorManager.fetchStartScreen()

    Navigator(
        screen = screenProvider.provideFeatureScreen(startScreen),
        onBackPressed = null,
        disposeBehavior = NavigatorDisposeBehavior(
            disposeNestedNavigators = true,
            disposeSteps = true,
        ),
    ) { navigator ->
        NestedNavigatorBackHandler(navigator, onBackPressed)
        DisposableEffect(Unit) {
            navigatorManager.attachNavigator(navigator)
            onDispose { navigatorManager.detachNavigator() }
        }
        content.invoke(navigator)
    }
}

@Composable
@InternalVoyagerApi
fun NestedNavigatorBackHandler(
    navigator: Navigator,
    onBackPressed: OnBackPressed
) {
    if (onBackPressed != null) {
        BackHandler(
            enabled = navigator.canPop || navigator.parent?.canPop ?: false,
            onBack = {
                if (onBackPressed(navigator.lastItem)) navigator.nestedPop()
            },
        )
    }
}