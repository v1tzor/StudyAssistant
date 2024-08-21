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

package ru.aleshin.studyassistant.settings.api.navigation

import ru.aleshin.studyassistant.core.common.inject.FeatureScreen
import ru.aleshin.studyassistant.settings.api.presentation.SettingsRootScreen

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
sealed class SettingsScreen : FeatureScreen<SettingsRootScreen> {
    data object General : SettingsScreen()
    data object Notification : SettingsScreen()
    data object Calendar : SettingsScreen()
    data object Subscription : SettingsScreen()
}