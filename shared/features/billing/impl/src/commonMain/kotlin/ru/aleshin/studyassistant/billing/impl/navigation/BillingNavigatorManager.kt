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

package ru.aleshin.studyassistant.billing.impl.navigation

import ru.aleshin.studyassistant.billing.api.navigation.BillingScreen
import ru.aleshin.studyassistant.billing.api.presentation.BillingRootScreen
import ru.aleshin.studyassistant.core.common.navigation.CommandBuffer
import ru.aleshin.studyassistant.core.common.navigation.NavigatorManager

/**
 * @author Stanislav Aleshin on 25.05.2024.
 */
internal interface BillingNavigatorManager : NavigatorManager<BillingScreen, BillingRootScreen> {
    class Base(commandBuffer: CommandBuffer) : BillingNavigatorManager,
        NavigatorManager.Abstract<BillingScreen, BillingRootScreen>(commandBuffer = commandBuffer)
}