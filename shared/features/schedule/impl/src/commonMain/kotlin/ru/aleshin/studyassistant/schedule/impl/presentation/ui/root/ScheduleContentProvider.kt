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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.backAnimation
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureManager
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleTheme
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.DetailsContent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.OverviewContent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.root.InternalScheduleFeatureComponent.Child
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.ShareContent

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal class ScheduleContentProvider(
    private val component: InternalScheduleFeatureComponent,
) : FeatureContentProvider {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun invoke(modifier: Modifier) {
        withDirectDI(directDI = { ScheduleFeatureManager.fetchDI() }) {
            ScheduleTheme {
                Children(
                    modifier = modifier,
                    stack = component.stack,
                    animation = backAnimation(
                        backHandler = component.backHandler,
                        onBack = component::navigateToBack
                    )
                ) { child ->
                    when (val instance = child.instance) {
                        is Child.OverviewChild -> {
                            OverviewContent(instance.component)
                        }
                        is Child.DetailsChild -> {
                            DetailsContent(instance.component)
                        }
                        is Child.ShareChild -> {
                            ShareContent(instance.component)
                        }
                    }
                }
            }
        }
    }
}