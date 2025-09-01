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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureManager
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
internal abstract class InternalScheduleFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<ScheduleConfig>,
    outputConsumer: OutputConsumer<ScheduleOutput>
) : ScheduleFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class OverviewChild(val component: OverviewComponent) : Child()
        data class DetailsChild(val component: DetailsComponent) : Child()
        data class ShareChild(val component: ShareComponent) : Child()
    }

    class Default(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<ScheduleConfig>,
        outputConsumer: OutputConsumer<ScheduleOutput>,
        private val overviewStoreFactory: OverviewComposeStore.Factory,
        private val detailsStoreFactory: DetailsComposeStore.Factory,
        private val shareStoreFactory: ShareComposeStore.Factory,
    ) : InternalScheduleFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        override val contentProvider = ScheduleContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<ScheduleConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = ScheduleConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(ScheduleConfig.Overview) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "SCHEDULE_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
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

        override fun onDestroyInstance() {
            ScheduleFeatureManager.finish()
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
                        outputConsumer = shareOutputConsumer(),
                        inputData = ShareInput(config.receivedShareId)
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
                is ShareOutput.NavigateToUserProfile -> {
                    val outputData = ScheduleOutput.NavigateToUserProfile(output.config.userId)
                    outputConsumer.consume(outputData)
                }
                is ShareOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}