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
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.Navigator

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
tailrec fun Navigator.root(): Navigator {
    return if (level == 0) this else checkNotNull(parent).root()
}

@OptIn(InternalVoyagerApi::class)
tailrec fun Navigator.nestedPop() {
    if (canPop) {
        pop()
    } else {
        dispose(lastItem)
        parent?.nestedPop()
    }
}

@OptIn(InternalVoyagerApi::class)
fun Navigator.disposeByKeys(keys: Set<ScreenKey>) {
    keys.forEach { targetKey ->
        val mockScreen = object : Screen {
            override val key: ScreenKey = targetKey

            @Composable
            override fun Content() = Unit
        }
        dispose(mockScreen)
    }
}