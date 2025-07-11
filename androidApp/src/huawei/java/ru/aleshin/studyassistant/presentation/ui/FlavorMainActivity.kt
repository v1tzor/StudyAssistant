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

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import io.github.vinceglb.filekit.core.FileKit
import org.kodein.di.instance
import ru.aleshin.studyassistant.android.R
import ru.aleshin.studyassistant.core.common.di.MainDependenciesGraph
import ru.aleshin.studyassistant.core.common.platform.PlatformActivity
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
abstract class FlavorMainActivity : PlatformActivity() {

    private val iapService by lazy {
        MainDependenciesGraph.fetchDI().instance<IapService>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_StudyAssistant)
        super.onCreate(savedInstanceState)
        iapService.init(this)

        if (savedInstanceState == null) {
            iapService.proceedIntent(intent, null)
        }

        FileKit.init(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        iapService.proceedIntent(intent, null)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        iapService.proceedIntent(data, requestCode)
    }
}