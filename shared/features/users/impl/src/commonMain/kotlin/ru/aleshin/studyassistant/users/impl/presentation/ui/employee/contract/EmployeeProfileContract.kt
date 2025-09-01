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

package ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures
import ru.aleshin.studyassistant.users.impl.presentation.models.EmployeeDetailsUi

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Serializable
internal data class EmployeeProfileState(
    val isLoading: Boolean = true,
    val employee: EmployeeDetailsUi? = null,
) : StoreState

internal sealed class EmployeeProfileEvent : StoreEvent {
    data class Started(val employeeId: UID) : EmployeeProfileEvent()
    data object ClickBack : EmployeeProfileEvent()
    data object ClickEdit : EmployeeProfileEvent()
}

internal sealed class EmployeeProfileEffect : StoreEffect {
    data class ShowError(val failures: UsersFailures) : EmployeeProfileEffect()
}

internal sealed class EmployeeProfileAction : StoreAction {
    data class UpdateEmployee(val employee: EmployeeDetailsUi?) : EmployeeProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : EmployeeProfileAction()
}

internal data class EmployeeProfileInput(
    val employeeId: UID
) : BaseInput

internal sealed class EmployeeProfileOutput : BaseOutput {
    data object NavigateToBack : EmployeeProfileOutput()
    data class NavigateToEmployeeEditor(val config: EditorConfig.Employee) : EmployeeProfileOutput()
}