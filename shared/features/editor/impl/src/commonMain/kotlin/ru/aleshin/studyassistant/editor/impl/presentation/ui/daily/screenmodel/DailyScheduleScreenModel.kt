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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleViewState

/**
 * @author Stanislav Aleshin on 14.07.2024
 */
internal class DailyScheduleScreenModel(
    private val workProcessor: DailyScheduleWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: DailyScheduleStateCommunicator,
    effectCommunicator: DailyScheduleEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<DailyScheduleViewState, DailyScheduleEvent, DailyScheduleAction, DailyScheduleEffect, DailyScheduleDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: DailyScheduleDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(DailyScheduleEvent.Init(deps.date, deps.baseScheduleId, deps.customScheduleId))
        }
    }

    override suspend fun WorkScope<DailyScheduleViewState, DailyScheduleAction, DailyScheduleEffect>.handleEvent(
        event: DailyScheduleEvent,
    ) {
        when (event) {
            is DailyScheduleEvent.Init -> with(event) {
                sendAction(DailyScheduleAction.UpdateTargetDate(date.mapEpochTimeToInstant()))
                launchBackgroundWork(BackgroundKey.SCHEDULES_WORK) {
                    val command = DailyScheduleWorkCommand.LoadSchedules(baseScheduleId, customScheduleId)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_SETTINGS) {
                    val command = DailyScheduleWorkCommand.LoadCalendarSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.CreateCustomSchedule -> with(state()) {
                launchBackgroundWork(BackgroundKey.SCHEDULES_WORK) {
                    val targetDate = checkNotNull(targetDate)
                    val command = DailyScheduleWorkCommand.CreateCustomSchedule(targetDate, baseSchedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.DeleteCustomSchedule -> with(state()) {
                launchBackgroundWork(BackgroundKey.SCHEDULES_WORK) {
                    val customScheduleId = checkNotNull(customSchedule?.uid)
                    val command = DailyScheduleWorkCommand.DeleteCustomSchedule(customScheduleId)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.SwapClasses -> with(state()) {
                launchBackgroundWork(BackgroundKey.EDIT_ACTION) {
                    val schedule = checkNotNull(customSchedule)
                    val command = DailyScheduleWorkCommand.SwapClasses(event.from, event.to, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.FastEditStartOfDay -> with(state()) {
                launchBackgroundWork(BackgroundKey.EDIT_ACTION) {
                    val schedule = checkNotNull(customSchedule)
                    val command = DailyScheduleWorkCommand.UpdateStartOfDay(event.time, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.FastEditClassesDuration -> with(state()) {
                launchBackgroundWork(BackgroundKey.EDIT_ACTION) {
                    val schedule = checkNotNull(customSchedule)
                    val command = DailyScheduleWorkCommand.UpdateClassesDuration(event.durations, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.FastEditBreaksDuration -> with(state()) {
                launchBackgroundWork(BackgroundKey.EDIT_ACTION) {
                    val schedule = checkNotNull(customSchedule)
                    val command = DailyScheduleWorkCommand.UpdateBreaksDuration(event.durations, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.DeleteClass -> with(state()) {
                launchBackgroundWork(BackgroundKey.CLASS_ACTION) {
                    val schedule = checkNotNull(customSchedule)
                    val command = DailyScheduleWorkCommand.DeleteClass(event.targetId, schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DailyScheduleEvent.CreateClassInEditor -> with(state()) {
                val maxNumberOfWeek = checkNotNull(calendarSettings).numberOfWeek
                val scheduleId = checkNotNull(customSchedule?.uid)
                val targetDate = checkNotNull(targetDate)
                val dayOfNumberedWeek = DayOfNumberedWeekUi(
                    dayOfWeek = targetDate.dateTime().dayOfWeek,
                    week = targetDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek),
                )
                val featureScreen = EditorScreen.Class(
                    classId = null,
                    scheduleId = scheduleId,
                    organizationId = null,
                    isCustomSchedule = true,
                    weekDay = dayOfNumberedWeek,
                )
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(DailyScheduleEffect.NavigateToLocal(targetScreen))
            }
            is DailyScheduleEvent.EditClassInEditor -> with(state()) {
                val maxNumberOfWeek = checkNotNull(calendarSettings).numberOfWeek
                val targetDate = checkNotNull(targetDate)
                val dayOfNumberedWeek = DayOfNumberedWeekUi(
                    dayOfWeek = targetDate.dateTime().dayOfWeek,
                    week = targetDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek),
                )
                val featureScreen = EditorScreen.Class(
                    classId = event.editClass.uid,
                    scheduleId = event.editClass.scheduleId,
                    organizationId = event.editClass.organization.uid,
                    isCustomSchedule = true,
                    weekDay = dayOfNumberedWeek,
                )
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(DailyScheduleEffect.NavigateToLocal(targetScreen))
            }
            is DailyScheduleEvent.NavigateToBack -> {
                sendEffect(DailyScheduleEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: DailyScheduleAction,
        currentState: DailyScheduleViewState,
    ) = when (action) {
        is DailyScheduleAction.UpdateSchedules -> currentState.copy(
            baseSchedule = action.baseSchedule,
            customSchedule = action.customSchedule,
            isLoading = false,
        )
        is DailyScheduleAction.UpdateCalendarSettings -> currentState.copy(
            calendarSettings = action.settings,
        )
        is DailyScheduleAction.UpdateTargetDate -> currentState.copy(
            targetDate = action.date,
        )
        is DailyScheduleAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        SCHEDULES_WORK, LOAD_SETTINGS, EDIT_ACTION, CLASS_ACTION
    }
}

@Composable
internal fun Screen.rememberDailyScheduleScreenModel(): DailyScheduleScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<DailyScheduleScreenModel>() }
}