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

package ru.aleshin.studyassistant.info.impl.presentation.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.backAnimation
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureManager
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.EmployeeContent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.OrganizationsContent
import ru.aleshin.studyassistant.info.impl.presentation.ui.root.InternalInfoFeatureComponent.Child
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.SubjectsContent
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoTheme

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
public class InfoContentProvider internal constructor(
    private val component: InternalInfoFeatureComponent,
) : FeatureContentProvider {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun invoke(modifier: Modifier) {
        withDirectDI(directDI = { InfoFeatureManager.fetchDI() }) {
            InfoTheme {
                Children(
                    modifier = modifier,
                    stack = component.stack,
                    animation = backAnimation(
                        backHandler = component.backHandler,
                        onBack = component::navigateToBack
                    )
                ) { child ->
                    when (val instance = child.instance) {
                        is Child.OrganizationsChild -> {
                            OrganizationsContent(instance.component)
                        }
                        is Child.EmployeeChild -> {
                            EmployeeContent(instance.component)
                        }
                        is Child.SubjectsChild -> {
                            SubjectsContent(instance.component)
                        }
                    }
                }
            }
        }
    }
}