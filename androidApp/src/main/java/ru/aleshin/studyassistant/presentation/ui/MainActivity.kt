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

package ru.aleshin.studyassistant.presentation.ui

import android.Manifest
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
import org.jetbrains.compose.resources.stringResource
import org.kodein.di.instance
import ru.aleshin.studyassistant.android.BuildConfig
import ru.aleshin.studyassistant.core.common.di.MainDependenciesGraph
import ru.aleshin.studyassistant.core.common.extensions.isAllowPermission
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.core.ui.ads.AdsConfiguration
import ru.aleshin.studyassistant.core.ui.resources.Res
import ru.aleshin.studyassistant.core.ui.resources.warning_granted_permission_message
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponentFactory
import ru.aleshin.studyassistant.widget.presentation.work.WidgetsUpdateScheduler

class MainActivity : FlavorMainActivity() {

    private val componentFactory = MainDependenciesGraph.fetchDI().instance<MainComponentFactory>()
    private lateinit var mainComponent: MainComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        mainComponent = componentFactory.createComponent(
            componentContext = defaultComponentContext(),
            deepLink = intent.dataString?.let(DeepLinkUrl::fromString),
        )

        setContent {
            AppScreen(
                component = mainComponent,
                adsConfiguration = AdsConfiguration(
                    tasksOverviewBannerId = BuildConfig.YANDEX_TASKS_BANNER_ID,
                    infoOrganizationsBannerId = BuildConfig.YANDEX_INFO_BANNER_ID,
                    aiQuotaRewardedId = BuildConfig.YANDEX_AI_REWARDED_ID,
                    scheduleImportRewardedId = BuildConfig.YANDEX_SCHEDULE_REWARDED_ID,
                )
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RequestNotificationPermission()
            }
        }
    }

    @Composable
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun RequestNotificationPermission() {
        val warningMessage = stringResource(Res.string.warning_granted_permission_message)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    override fun onPause() {
        WidgetsUpdateScheduler.enqueueImmediate(this)
        super.onPause()
    }

    private fun handleDeepLink(intent: Intent) {
        if (::mainComponent.isInitialized) {
            intent.dataString?.let(DeepLinkUrl::fromString)?.let(mainComponent::handleDeepLink)
        }
    }
}
