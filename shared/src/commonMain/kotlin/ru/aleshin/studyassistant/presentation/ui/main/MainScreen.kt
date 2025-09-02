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

package ru.aleshin.studyassistant.presentation.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.navigation.backAnimation
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantTheme
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEvent
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent
import ru.aleshin.studyassistant.presentation.ui.splash.SplashContent
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.TabsContent

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
@OptIn(ExperimentalDecomposeApi::class)
fun MainScreen(
    mainComponent: MainComponent,
    modifier: Modifier = Modifier,
) {
    val store = mainComponent.store
    val state = store.stateAsState()

    StudyAssistantTheme(
        themeType = state.value.generalSettings.themeType,
        languageType = state.value.generalSettings.languageType,
    ) {
        val coreStrings = StudyAssistantRes.strings
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        ) { paddingValues ->
            ChildStack(
                modifier = Modifier.padding(paddingValues),
                stack = mainComponent.stack,
                animation = backAnimation(
                    backHandler = mainComponent.backHandler,
                    onBack = mainComponent::navigateToBack,
                ),
            ) { child ->
                when (val instance = child.instance) {
                    is MainComponent.Child.SplashChild -> {
                        SplashContent()
                        LaunchedEffect(child.configuration) {
                            store.dispatchEvent(MainEvent.ExecuteNavigation)
                        }
                    }
                    is MainComponent.Child.TabNavigationChild -> {
                        TabsContent(instance.component)
                    }
                    is MainComponent.Child.AuthChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                    is MainComponent.Child.EditorChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                    is MainComponent.Child.PreviewChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                    is MainComponent.Child.ScheduleChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                    is MainComponent.Child.BillingChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                    is MainComponent.Child.SettingsChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                    is MainComponent.Child.UsersChild -> {
                        instance.component.contentProvider.invoke(Modifier)
                    }
                }

                store.handleEffects { effect ->
                    when (effect) {
                        is MainEffect.ShowError -> snackbarState.showSnackbar(
                            message = effect.failures.mapToMessage(coreStrings),
                            duration = SnackbarDuration.Indefinite,
                        )
                    }
                }
            }
        }
    }
}