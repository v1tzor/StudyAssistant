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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksInput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksState

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
internal class HomeworksComposeStore(
    private val workProcessor: HomeworksDetailsWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<HomeworksState>,
    effectCommunicator: EffectCommunicator<HomeworksEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<HomeworksState, HomeworksEvent, HomeworksAction, HomeworksEffect, HomeworksInput, HomeworksOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: HomeworksInput, isRestore: Boolean) {
        dispatchEvent(HomeworksEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<HomeworksState, HomeworksAction, HomeworksEffect, HomeworksOutput>.handleEvent(
        event: HomeworksEvent,
    ) {
        when (event) {
            is HomeworksEvent.Started -> with(event) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                if (!isRestore) {
                    val targetTimeRange = TimeRange(
                        from = currentDate.startOfWeek().shiftWeek(-1),
                        to = currentDate.endOfWeek().shiftWeek(+1),
                    )
                    sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                    launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                        val scrollDate = event.inputData.targetDate?.mapEpochTimeToInstant() ?: currentDate
                        val command = HomeworksDetailsWorkCommand.LoadHomeworks(targetTimeRange, scrollDate)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    val selectedTimeRange = state.selectedTimeRange ?: return
                    launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                        val command = HomeworksDetailsWorkCommand.LoadHomeworks(selectedTimeRange)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
                launchBackgroundWork(BackgroundKey.LOAD_FRIENDS) {
                    val command = HomeworksDetailsWorkCommand.LoadFriends
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ACTIVE_SCHEDULE) {
                    val command = HomeworksDetailsWorkCommand.LoadActiveSchedule(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_USER_STATUS) {
                    val command = HomeworksDetailsWorkCommand.LoadPaidUserStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.ScheduleGoal -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = HomeworksDetailsWorkCommand.ScheduleGoal(goalCreateModel)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.DeleteGoal -> with(event) {
                launchBackgroundWork(BackgroundKey.GOAL_ACTION) {
                    val command = HomeworksDetailsWorkCommand.DeleteGoal(goal)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.ShareHomeworks -> with(event) {
                launchBackgroundWork(BackgroundKey.SHARE_HOMEWORK) {
                    val command = HomeworksDetailsWorkCommand.ShareHomeworks(sentMediatedHomeworks)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.ClickCurrentTimeRange -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val targetTimeRange = TimeRange(
                    from = currentDate.startOfWeek().shiftWeek(-1),
                    to = currentDate.endOfWeek().shiftWeek(+1),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksDetailsWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.ClickNextTimeRange -> with(state()) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.to,
                    to = currentTimeRange.to.shiftWeek(+3),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksDetailsWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.ClickPreviousTimeRange -> with(state()) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.from.shiftWeek(-3),
                    to = currentTimeRange.from,
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksDetailsWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.DoHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = true, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksDetailsWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.SkipHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = false, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksDetailsWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.RepeatHomework -> with(event) {
                val updatedHomework = homework.copy(isDone = false, completeDate = null)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksDetailsWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.ClickEditHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                consumeOutput(HomeworksOutput.NavigateToHomeworkEditor(config))
            }
            is HomeworksEvent.ClickAddHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = null,
                    date = date.startThisDay().toEpochMilliseconds(),
                    subjectId = null,
                    organizationId = null,
                )
                consumeOutput(HomeworksOutput.NavigateToHomeworkEditor(config))
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
                val config = EditorConfig.Homework(
                    homeworkId = null,
                    date = null,
                    subjectId = activeClass?.subject?.uid,
                    organizationId = activeClass?.organization?.uid,
                )
                consumeOutput(HomeworksOutput.NavigateToHomeworkEditor(config))
            }
            is HomeworksEvent.ClickBack -> {
                consumeOutput(HomeworksOutput.NavigateToBack)
            }
            is HomeworksEvent.ClickPaidFunction -> {
                consumeOutput(HomeworksOutput.NavigateToBilling)
            }
        }
    }

    override suspend fun reduce(
        action: HomeworksAction,
        currentState: HomeworksState,
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
        is HomeworksAction.UpdateUserPaidStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
        is HomeworksAction.UpdateFriends -> currentState.copy(
            friends = action.friends,
        )
        is HomeworksAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORKS,
        LOAD_FRIENDS,
        LOAD_ACTIVE_SCHEDULE,
        LOAD_PAID_USER_STATUS,
        HOMEWORK_ACTION,
        GOAL_ACTION,
        SHARE_HOMEWORK
    }

    class Factory(
        private val workProcessor: HomeworksDetailsWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<HomeworksComposeStore, HomeworksState> {

        override fun create(savedState: HomeworksState): HomeworksComposeStore {
            return HomeworksComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}