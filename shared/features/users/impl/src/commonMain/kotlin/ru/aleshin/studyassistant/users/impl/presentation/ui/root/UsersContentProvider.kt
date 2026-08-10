/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.users.impl.presentation.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import org.kodein.di.DI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.backAnimation
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.EmployeeProfileContent
import ru.aleshin.studyassistant.users.impl.presentation.ui.root.UsersFeatureComponent.Child

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal class UsersContentProvider(
    di: DI,
    private val component: UsersFeatureComponent,
) : FeatureContentProvider(di) {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun RootContent(modifier: Modifier) {
        ChildStack(
            modifier = modifier,
            stack = component.stack,
            animation = backAnimation(
                backHandler = component.backHandler,
                onBack = component::navigateToBack
            )
        ) { child ->
            when (val instance = child.instance) {
                is Child.EmployeeProfileChild -> {
                    EmployeeProfileContent(instance.component)
                }
            }
        }
    }
}
