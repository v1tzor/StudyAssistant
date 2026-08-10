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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.schedule.api.ScheduleConfig
import ru.aleshin.studyassistant.schedule.api.ScheduleOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store.DetailsComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store.DetailsComposeStore
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store.OverviewComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store.OverviewComposeStore
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store.ShareComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store.ShareComposeStore

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
internal abstract class ScheduleFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: ScheduleConfig,
    protected val outputConsumer: OutputConsumer<ScheduleOutput>,
) : FeatureComponent<ScheduleConfig, ScheduleOutput>(componentContext) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class OverviewChild(val component: OverviewComponent) : Child()
        data class DetailsChild(val component: DetailsComponent) : Child()
        data class ShareChild(val component: ShareComponent) : Child()
    }

    class Default(
        componentContext: ComponentContext,
        startConfig: ScheduleConfig,
        outputConsumer: OutputConsumer<ScheduleOutput>,
        private val overviewStoreFactory: OverviewComposeStore.Factory,
        private val detailsStoreFactory: DetailsComposeStore.Factory,
        private val shareStoreFactory: ShareComposeStore.Factory,
    ) : ScheduleFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        private val stackNavigation = StackNavigation<ScheduleConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = ScheduleConfig.serializer(),
            initialStack = { listOf(startConfig) },
            key = STACK_KEY,
            handleBackButton = true,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "SCHEDULE_ROOT_STACK"
        }

        override fun navigateToBack() {
            if (stack.active.instance is Child.ShareChild) {
                stackNavigation.pop()
                outputConsumer.consume(ScheduleOutput.NavigateToBack)
            } else {
                stackNavigation.pop { isPop ->
                    if (!isPop) outputConsumer.consume(ScheduleOutput.NavigateToBack)
                }
            }
        }

        private fun createChild(
            config: ScheduleConfig,
            componentContext: ComponentContext
        ): Child {
            return when (config) {
                is ScheduleConfig.Overview -> Child.OverviewChild(
                    component = OverviewComponent.Default(
                        storeFactory = overviewStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = overviewOutputConsumer(),
                    )
                )
                is ScheduleConfig.Details -> Child.DetailsChild(
                    component = DetailsComponent.Default(
                        storeFactory = detailsStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = detailsOutputConsumer(),
                    )
                )
                is ScheduleConfig.Share -> Child.ShareChild(
                    component = ShareComponent.Default(
                        storeFactory = shareStoreFactory,
                        componentContext = componentContext,
                        inputData = ShareInput(config.code),
                        outputConsumer = shareOutputConsumer()
                    )
                )
            }
        }

        private fun overviewOutputConsumer() = OutputConsumer<OverviewOutput> { output ->
            when (output) {
                is OverviewOutput.NavigateToDetails -> {
                    stackNavigation.pushToFront(ScheduleConfig.Details)
                }
                is OverviewOutput.NavigateToHomeworkEditor -> {
                    val outputData = ScheduleOutput.NavigateToEditor.Homework(
                        homeworkId = output.config.homeworkId,
                        date = output.config.date,
                        subjectId = output.config.subjectId,
                        organizationId = output.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }
                is OverviewOutput.NavigateToDailyScheduleEditor -> {
                    val outputData = ScheduleOutput.NavigateToEditor.DailySchedule(
                        date = output.config.date,
                        customScheduleId = output.config.customScheduleId,
                        baseScheduleId = output.config.baseScheduleId,
                    )
                    outputConsumer.consume(outputData)
                }
            }
        }

        private fun detailsOutputConsumer() = OutputConsumer<DetailsOutput> { output ->
            when (output) {
                is DetailsOutput.NavigateToOverview -> {
                    stackNavigation.pushToFront(ScheduleConfig.Overview)
                }
                is DetailsOutput.NavigateToHomeworkEditor -> {
                    val outputData = ScheduleOutput.NavigateToEditor.Homework(
                        homeworkId = output.config.homeworkId,
                        date = output.config.date,
                        subjectId = output.config.subjectId,
                        organizationId = output.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }
                is DetailsOutput.NavigateToWeekScheduleEditor -> {
                    val outputData = ScheduleOutput.NavigateToEditor.WeekSchedule(
                        week = output.config.week,
                    )
                    outputConsumer.consume(outputData)
                }
            }
        }

        private fun shareOutputConsumer() = OutputConsumer<ShareOutput> { output ->
            when (output) {
                is ShareOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}
