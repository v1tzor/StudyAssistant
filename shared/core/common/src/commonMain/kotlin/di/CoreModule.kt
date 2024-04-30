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

package di

import managers.CoroutineManager
import managers.DateManager
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import payments.SubscriptionChecker

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
val coreModule = DI.Module("Core") {
    bindSingleton<CoroutineManager> { CoroutineManager.Base() }
    bindSingleton<DateManager> { DateManager.Base() }
    bindSingleton<SubscriptionChecker> { SubscriptionChecker.FreeApp }
}