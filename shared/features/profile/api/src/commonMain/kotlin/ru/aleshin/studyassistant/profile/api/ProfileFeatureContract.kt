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

package ru.aleshin.studyassistant.profile.api

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
@Serializable
public sealed class ProfileConfig {

    @Serializable
    public data object Profile : ProfileConfig()
}

public sealed class ProfileOutput : BaseOutput {
    public data object NavigateToBack : ProfileOutput()
    public data object NavigateToProfileEditor : ProfileOutput()
    public data object NavigateToScheduleSharing : ProfileOutput()
    public sealed class NavigateToSettings : ProfileOutput() {
        public data object General : NavigateToSettings()
        public data object Notification : NavigateToSettings()
        public data object Calendar : NavigateToSettings()
        public data object Ai : NavigateToSettings()
        public data object AboutApp : NavigateToSettings()
    }
}
