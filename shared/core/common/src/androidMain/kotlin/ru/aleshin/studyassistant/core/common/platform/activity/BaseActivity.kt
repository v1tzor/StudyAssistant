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
package ru.aleshin.studyassistant.core.common.platform.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import ru.aleshin.studyassistant.core.common.architecture.BaseViewModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.ScreenDependencies
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
abstract class BaseActivity<S : BaseViewState, E : BaseEvent, A : BaseAction, F : BaseUiEffect, D : ScreenDependencies> : ComponentActivity() {

    protected val viewModel by lazy {
        ViewModelProvider(this, fetchViewModelFactory())[fetchViewModelClass()]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()
        super.onCreate(savedInstanceState)
        setContent { Content() }
    }

    open fun initDI() {}

    @Composable
    abstract fun Content()

    abstract fun fetchViewModelFactory(): ViewModelProvider.Factory

    abstract fun fetchViewModelClass(): Class<out BaseViewModel<S, E, A, F, D>>
}
