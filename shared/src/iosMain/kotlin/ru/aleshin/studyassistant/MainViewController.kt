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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.MainDependenciesGraph
import ru.aleshin.studyassistant.presentation.ui.main.MainScreen
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponentFactory

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@OptIn(ExperimentalDecomposeApi::class)
fun MainViewController(
    componentContext: ComponentContext,
    backDispatcher: BackDispatcher,
) = ComposeUIViewController {
    val componentFactory = remember {
        MainDependenciesGraph.fetchDI().instance<MainComponentFactory>()
    }
    val mainComponent = remember {
        componentFactory.createComponent(componentContext)
    }
    PredictiveBackGestureOverlay(
        backDispatcher = backDispatcher,
        backIcon = null,
        modifier = Modifier.fillMaxSize(),
    ) {
        MainScreen(mainComponent = mainComponent)
    }
}