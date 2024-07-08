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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.screenmodel

import ru.aleshin.studyassistant.core.common.architecture.communications.state.StateCommunicator
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface OverviewStateCommunicator : StateCommunicator<OverviewViewState> {
    class Base : OverviewStateCommunicator, StateCommunicator.Abstract<OverviewViewState>(
        defaultState = OverviewViewState()
    )
}