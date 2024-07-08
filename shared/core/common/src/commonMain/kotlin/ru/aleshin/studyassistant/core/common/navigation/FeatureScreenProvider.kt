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

package ru.aleshin.studyassistant.core.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.FeatureScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
interface FeatureScreenProvider<S : FeatureScreen> {
    fun provideFeatureScreen(screen: S): Screen
}

@Composable
inline fun <reified T : FeatureScreenProvider<S>, S : FeatureScreen> Screen.rememberScreenProvider(): T {
    val di = localDI().direct
    return remember { di.instance<T>() }
}