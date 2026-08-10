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

package ru.aleshin.studyassistant.preview.api

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
@Serializable
public sealed class PreviewConfig {

    @Serializable
    public data object Intro : PreviewConfig()

    @Serializable
    public data object Setup : PreviewConfig()
}

public sealed class PreviewOutput : BaseOutput {
    public data object NavigateToApp : PreviewOutput()
    public data object NavigateToWeekScheduleEditor : PreviewOutput()
}
