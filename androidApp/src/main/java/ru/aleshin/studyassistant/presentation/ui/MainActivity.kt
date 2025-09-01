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

package ru.aleshin.studyassistant.presentation.ui

import android.Manifest
import android.app.ComponentCaller
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.arkivanov.decompose.defaultComponentContext
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.MainDependenciesGraph
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.extensions.isAllowPermission
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponentFactory

class MainActivity : FlavorMainActivity() {

    private val componentFactory = MainDependenciesGraph.fetchDI().instance<MainComponentFactory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val mainComponent = componentFactory.createComponent(
            componentContext = defaultComponentContext(),
            deepLink = null,
        )

        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RequestNotificationPermission()
            }
            AppScreen(mainComponent)
        }
    }

    @Composable
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun RequestNotificationPermission() {
        val coreStrings = fetchCoreStrings(fetchAppLanguage(fetchCurrentLanguage()))
        val warningMessage = coreStrings.warningGrantedPermissionMessage
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this@MainActivity, warningMessage, Toast.LENGTH_LONG).show()
            }
        }

        SideEffect {
            if (!isAllowPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
    }
}