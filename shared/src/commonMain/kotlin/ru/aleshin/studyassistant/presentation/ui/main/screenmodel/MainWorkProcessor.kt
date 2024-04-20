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

package ru.aleshin.studyassistant.presentation.ui.main.screenmodel

import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.FlowWorkResult
import architecture.screenmodel.work.WorkCommand
import architecture.screenmodel.work.WorkResult
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.navigation.MainScreenProvider
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainAction
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect

/**
 * @author Stanislav Aleshin on 19.04.2024.
 */
interface MainWorkProcessor : FlowWorkProcessor<MainWorkCommand, MainAction, MainEffect> {

    class Base(
        private val screenProvider: MainScreenProvider,
    ) : MainWorkProcessor {

        override suspend fun work(command: MainWorkCommand) = when(command) {
            MainWorkCommand.StartedChecks -> startedChecksWork()
            MainWorkCommand.LoadThemeSettings -> loadThemeWork()
        }

        private fun loadThemeWork() = flow<WorkResult<MainAction, MainEffect>> {
            // TODO: Load theme settings
        }

        private fun startedChecksWork() = flow<WorkResult<MainAction, MainEffect>> {
            // TODO: Check on auth and first start
        }
    }
}

sealed class MainWorkCommand : WorkCommand {
    data object StartedChecks : MainWorkCommand()
    data object LoadThemeSettings : MainWorkCommand()
}