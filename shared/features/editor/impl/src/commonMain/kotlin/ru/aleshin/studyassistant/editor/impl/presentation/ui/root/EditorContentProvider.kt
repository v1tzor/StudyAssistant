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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.backAnimation
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureManager
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorTheme
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.ClassContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.DailyScheduleContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.EmployeeContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.HomeworkContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.OrganizationContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.ProfileContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.root.InternalEditorFeatureComponent.Child
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.WeekScheduleContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.SubjectContent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.TodoContent

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
public class EditorContentProvider internal constructor(
    private val component: InternalEditorFeatureComponent,
) : FeatureContentProvider {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun invoke(modifier: Modifier) {
        withDirectDI(directDI = { EditorFeatureManager.fetchDI() }) {
            EditorTheme {
                ChildStack(
                    modifier = modifier,
                    stack = component.stack,
                    animation = backAnimation(
                        backHandler = component.backHandler,
                        onBack = component::navigateToBack
                    )
                ) { child ->
                    when (val instance = child.instance) {
                        is Child.ClassChild -> {
                            ClassContent(instance.component)
                        }
                        is Child.DailyScheduleChild -> {
                            DailyScheduleContent(instance.component)
                        }
                        is Child.EmployeeChild -> {
                            EmployeeContent(instance.component)
                        }
                        is Child.HomeworkChild -> {
                            HomeworkContent(instance.component)
                        }
                        is Child.OrganizationChild -> {
                            OrganizationContent(instance.component)
                        }
                        is Child.ProfileChild -> {
                            ProfileContent(instance.component)
                        }
                        is Child.SubjectChild -> {
                            SubjectContent(instance.component)
                        }
                        is Child.TodoChild -> {
                            TodoContent(instance.component)
                        }
                        is Child.WeekScheduleChild -> {
                            WeekScheduleContent(instance.component)
                        }
                    }
                }
            }
        }
    }
}