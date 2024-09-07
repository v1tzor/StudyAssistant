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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksDeps
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksViewState

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
internal class HomeworksScreenModel(
    private val workProcessor: HomeworksWorkProcessor,
    private val screenProvider: TasksScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: HomeworksStateCommunicator,
    effectCommunicator: HomeworksEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<HomeworksViewState, HomeworksEvent, HomeworksAction, HomeworksEffect, HomeworksDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: HomeworksDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(HomeworksEvent.Init)
        }
    }

    override suspend fun WorkScope<HomeworksViewState, HomeworksAction, HomeworksEffect>.handleEvent(
        event: HomeworksEvent,
    ) {
        when (event) {
            is HomeworksEvent.Init -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val targetTimeRange = TimeRange(
                    from = currentDate.startOfWeek().shiftWeek(-1),
                    to = currentDate.endOfWeek().shiftWeek(+1),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ACTIVE_SCHEDULE) {
                    val command = HomeworksWorkCommand.LoadActiveSchedule(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.CurrentTimeRange -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val targetTimeRange = TimeRange(
                    from = currentDate.startOfWeek().shiftWeek(-1),
                    to = currentDate.endOfWeek().shiftWeek(+1),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.NextTimeRange -> with(state()) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.to,
                    to = currentTimeRange.to.shiftWeek(+3),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.PreviousTimeRange -> with(state()) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.from.shiftWeek(-3),
                    to = currentTimeRange.from,
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.DoHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = true, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.SkipHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = false, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.RepeatHomework -> with(event) {
                val updatedHomework = homework.copy(isDone = false, completeDate = null)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.NavigateToHomeworkEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(HomeworksEffect.NavigateToGlobal(screen))
            }
            is HomeworksEvent.AddHomeworkInEditor -> with(state()) {
                val currentTime = dateManager.fetchCurrentInstant()
                val activeClass = if (activeSchedule != null && activeSchedule.classes.isNotEmpty()) {
                    val dailyTimeRange = TimeRange(
                        from = activeSchedule.classes.first().timeRange.from,
                        to = activeSchedule.classes.last().timeRange.to.shiftMinutes(10),
                    )
                    if (dailyTimeRange.containsTime(currentTime)) {
                        activeSchedule.classes.findLast { classModel ->
                            val firstFilter = classModel.timeRange.to.dateTime().time < currentTime.dateTime().time
                            val secondFilter = classModel.timeRange.containsTime(currentTime)
                            return@findLast firstFilter || secondFilter
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
                val featureScreen = EditorScreen.Homework(
                    homeworkId = null,
                    date = null,
                    subjectId = activeClass?.subject?.uid,
                    organizationId = activeClass?.organization?.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(HomeworksEffect.NavigateToGlobal(screen))
            }
            is HomeworksEvent.NavigateToBack -> {
                sendEffect(HomeworksEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: HomeworksAction,
        currentState: HomeworksViewState,
    ) = when (action) {
        is HomeworksAction.UpdateHomeworks -> currentState.copy(
            homeworks = action.homeworks,
            isLoading = false,
        )
        is HomeworksAction.UpdateActiveSchedule -> currentState.copy(
            activeSchedule = action.activeSchedule,
        )
        is HomeworksAction.UpdateDates -> currentState.copy(
            currentDate = action.currentDate,
            selectedTimeRange = action.selectedTimeRange,
        )
        is HomeworksAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORKS, LOAD_ACTIVE_SCHEDULE, HOMEWORK_ACTION,
    }
}

@Composable
internal fun Screen.rememberHomeworksScreenModel(): HomeworksScreenModel {
    val di = TasksFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<HomeworksScreenModel>() }
}