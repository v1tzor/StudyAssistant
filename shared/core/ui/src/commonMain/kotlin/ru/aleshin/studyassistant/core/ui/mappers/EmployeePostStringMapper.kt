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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.post_director as core_post_director
import ru.aleshin.studyassistant.core.ui.resources.post_employee as core_post_employee
import ru.aleshin.studyassistant.core.ui.resources.post_manager as core_post_manager
import ru.aleshin.studyassistant.core.ui.resources.post_mentor as core_post_mentor
import ru.aleshin.studyassistant.core.ui.resources.post_teacher as core_post_teacher
import ru.aleshin.studyassistant.core.ui.resources.post_tutor as core_post_tutor

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
fun EmployeePost.mapToString() = when (this) {
    EmployeePost.EMPLOYEE -> stringResource(CoreRes.string.core_post_employee)
    EmployeePost.TEACHER -> stringResource(CoreRes.string.core_post_teacher)
    EmployeePost.DIRECTOR -> stringResource(CoreRes.string.core_post_director)
    EmployeePost.TUTOR -> stringResource(CoreRes.string.core_post_tutor)
    EmployeePost.MENTOR -> stringResource(CoreRes.string.core_post_mentor)
    EmployeePost.MANAGER -> stringResource(CoreRes.string.core_post_manager)
}