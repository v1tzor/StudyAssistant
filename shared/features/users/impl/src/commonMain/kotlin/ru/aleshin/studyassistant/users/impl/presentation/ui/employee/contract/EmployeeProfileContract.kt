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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures
import ru.aleshin.studyassistant.users.impl.presentation.models.EmployeeDetailsUi

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Immutable
@Parcelize
internal data class EmployeeProfileViewState(
    val isLoading: Boolean = true,
    val employee: EmployeeDetailsUi? = null,
) : BaseViewState

internal sealed class EmployeeProfileEvent : BaseEvent {
    data class Init(val employeeId: UID) : EmployeeProfileEvent()
    data object NavigateToBack : EmployeeProfileEvent()
    data object NavigateToEditor : EmployeeProfileEvent()
}

internal sealed class EmployeeProfileEffect : BaseUiEffect {
    data class ShowError(val failures: UsersFailures) : EmployeeProfileEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : EmployeeProfileEffect()
    data object NavigateToBack : EmployeeProfileEffect()
}

internal sealed class EmployeeProfileAction : BaseAction {
    data class UpdateEmployee(val employee: EmployeeDetailsUi?) : EmployeeProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : EmployeeProfileAction()
}