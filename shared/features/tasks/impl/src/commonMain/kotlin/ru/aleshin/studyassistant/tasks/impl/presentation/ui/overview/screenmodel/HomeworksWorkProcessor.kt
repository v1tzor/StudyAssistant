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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ShareHomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.UsersInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect

/**
 * @author Stanislav Aleshin on 12.06.2025.
 */
internal interface HomeworksWorkProcessor : FlowWorkProcessor<HomeworksWorkCommand, OverviewAction, OverviewEffect> {

    class Base(
        private val homeworksInteractor: HomeworksInteractor,
        private val shareInteractor: ShareHomeworksInteractor,
        private val scheduleInteractor: ScheduleInteractor,
        private val usersInteractor: UsersInteractor,
    ) : HomeworksWorkProcessor {

        override suspend fun work(command: HomeworksWorkCommand) = when (command) {
            is HomeworksWorkCommand.LoadHomeworks -> loadHomeworksWork(command.currentDate)
            is HomeworksWorkCommand.LoadHomeworksProgress -> loadHomeworksProgressWork(command.currentDate)
            is HomeworksWorkCommand.LoadSharedHomeworks -> loadSharedHomeworksWork()
            is HomeworksWorkCommand.LoadActiveSchedule -> loadActiveScheduleWork(command.currentDate)
            is HomeworksWorkCommand.LoadPaidUserStatus -> loadUserPaidStatusWork()
            is HomeworksWorkCommand.DoHomework -> doHomeworkWork(command.homework)
            is HomeworksWorkCommand.RepeatHomework -> repeatHomeworkWork(command.homework)
            is HomeworksWorkCommand.SkipHomework -> skipHomeworkWork(command.homework)
            is HomeworksWorkCommand.ShareHomeworks -> shareHomeworksWork(command.sentMediatedHomeworks)
        }

        private fun loadHomeworksWork(currentDate: Instant) = flow<OverviewWorkResult> {
            val targetTimeRange = TimeRange(
                from = currentDate.startOfWeek().shiftWeek(-1),
                to = currentDate.endOfWeek().shiftWeek(+1),
            )
            homeworksInteractor.fetchHomeworksByTimeRange(targetTimeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { homeworks ->
                    val homeworksMap = homeworks.mapValues { it.value.mapToUi() }
                    val homeworksScope = homeworksInteractor.calculateHomeworkScope(homeworks).mapToUi()
                    emit(ActionResult(OverviewAction.UpdateHomeworks(homeworksMap, homeworksScope)))
                },
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateHomeworksLoading(true)))
        }

        private fun loadHomeworksProgressWork(currentDate: Instant) = flow<OverviewWorkResult> {
            homeworksInteractor.fetchHomeworksProgress(currentDate.startThisDay()).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = {
                    emit(ActionResult(OverviewAction.UpdateHomeworksProgress(it.mapToUi())))
                },
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateHomeworksProgressLoading(true)))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadSharedHomeworksWork() = flow {
            val sharedHomeworksFlow = shareInteractor.fetchSharedHomeworksDetails()
            val friendsFlow = usersInteractor.fetchAllFriends()

            sharedHomeworksFlow.flatMapLatestWithResult(
                secondFlow = friendsFlow,
                onError = { OverviewEffect.ShowError(it) },
                onData = { homeworks, friends ->
                    val allFriends = friends.map { it.mapToUi() }
                    OverviewAction.UpdateSharedHomeworks(homeworks.mapToUi(), allFriends)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateShareLoading(true)))
        }

        private fun loadActiveScheduleWork(currentDate: Instant) = flow<OverviewWorkResult> {
            scheduleInteractor.fetchScheduleByDate(currentDate).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { schedule ->
                    emit(ActionResult(OverviewAction.UpdateActiveSchedule(schedule.mapToUi())))
                },
            )
        }

        private fun loadUserPaidStatusWork() = flow {
            usersInteractor.fetchAppUserPaidStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(OverviewAction.UpdateUserPaidStatus(it))) },
            )
        }

        private fun doHomeworkWork(homework: HomeworkUi) = flow {
            homeworksInteractor.doHomework(homework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun skipHomeworkWork(homework: HomeworkUi) = flow {
            homeworksInteractor.skipHomework(homework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun repeatHomeworkWork(homework: HomeworkUi) = flow {
            val updatedHomework = homework.copy(isDone = false, completeDate = null)
            homeworksInteractor.updateHomework(updatedHomework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun shareHomeworksWork(sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) = flow {
            shareInteractor.shareHomeworks(sentMediatedHomeworks.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }
    }
}

internal sealed class HomeworksWorkCommand : WorkCommand {
    data class LoadHomeworks(val currentDate: Instant) : HomeworksWorkCommand()
    data object LoadSharedHomeworks : HomeworksWorkCommand()
    data class LoadActiveSchedule(val currentDate: Instant) : HomeworksWorkCommand()
    data class LoadHomeworksProgress(val currentDate: Instant) : HomeworksWorkCommand()
    data object LoadPaidUserStatus : HomeworksWorkCommand()
    data class DoHomework(val homework: HomeworkUi) : HomeworksWorkCommand()
    data class SkipHomework(val homework: HomeworkUi) : HomeworksWorkCommand()
    data class RepeatHomework(val homework: HomeworkUi) : HomeworksWorkCommand()
    data class ShareHomeworks(val sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) : HomeworksWorkCommand()
}