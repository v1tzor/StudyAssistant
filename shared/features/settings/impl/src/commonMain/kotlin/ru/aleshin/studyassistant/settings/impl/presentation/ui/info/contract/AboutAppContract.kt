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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.EmptyUiEffect

/**
 * @author Stanislav Aleshin on 04.08.2025
 */
@Immutable
@Parcelize
internal sealed class AboutAppViewState : BaseViewState {
    data object Base : AboutAppViewState()
}

internal sealed class AboutAppEvent : BaseEvent

internal sealed class AboutAppEffect : EmptyUiEffect

internal sealed class AboutAppAction : BaseAction