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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.backAnimation
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureManager
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewTheme
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.IntroContent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.root.InternalPreviewFeatureComponent.Child
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.SetupContent

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
public class PreviewContentProvider internal constructor(
    private val component: InternalPreviewFeatureComponent,
) : FeatureContentProvider {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun invoke(modifier: Modifier) {
        withDirectDI(directDI = { PreviewFeatureManager.fetchDI() }) {
            PreviewTheme {
                ChildStack(
                    modifier = modifier,
                    stack = component.stack,
                    animation = backAnimation(
                        backHandler = component.backHandler,
                        onBack = component::navigateToBack
                    )
                ) { child ->
                    when (val instance = child.instance) {
                        is Child.IntroChild -> {
                            IntroContent(instance.component)
                        }
                        is Child.SetupChild -> {
                            SetupContent(instance.component)
                        }
                    }
                }
            }
        }
    }
}