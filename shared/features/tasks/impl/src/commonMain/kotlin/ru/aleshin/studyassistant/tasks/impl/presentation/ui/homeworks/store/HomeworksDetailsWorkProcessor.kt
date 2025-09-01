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

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.GoalsInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ShareHomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.UsersInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksOutput

/**
 * @author Stanislav Aleshin on 27.06.2024.
 */
internal interface HomeworksDetailsWorkProcessor :
    FlowWorkProcessor<HomeworksDetailsWorkCommand, HomeworksAction, HomeworksEffect, HomeworksOutput> {

    class Base(
        private val homeworksInteractor: HomeworksInteractor,
        private val scheduleInteractor: ScheduleInteractor,
        private val shareInteractor: ShareHomeworksInteractor,
        private val usersInteractor: UsersInteractor,
        private val goalsInteractor: GoalsInteractor,
    ) : HomeworksDetailsWorkProcessor {

        override suspend fun work(command: HomeworksDetailsWorkCommand) = when (command) {
            is HomeworksDetailsWorkCommand.LoadHomeworks -> loadHomeworksWork(command.timeRange, command.scrollDate)
            is HomeworksDetailsWorkCommand.LoadActiveSchedule -> loadActiveScheduleWork(command.currentDate)
            is HomeworksDetailsWorkCommand.LoadPaidUserStatus -> loadUserPaidStatusWork()
            is HomeworksDetailsWorkCommand.LoadFriends -> loadFriendsWork()
            is HomeworksDetailsWorkCommand.UpdateHomework -> updateHomeworkWork(command.homework)
            is HomeworksDetailsWorkCommand.ShareHomeworks -> shareHomeworksWork(command.sentMediatedHomeworks)
            is HomeworksDetailsWorkCommand.ScheduleGoal -> scheduleGoalWork(command.goalCreateModel)
            is HomeworksDetailsWorkCommand.DeleteGoal -> deleteGoalWork(command.goal)
        }

        private fun loadHomeworksWork(timeRange: TimeRange, scrollDate: Instant?) = flow<HomeworksWorkResult> {
            var isScrolled = false
            homeworksInteractor.fetchHomeworksByTimeRange(timeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { homeworks ->
                    val homeworksMap = homeworks.mapValues { it.value.mapToUi() }
                    emit(ActionResult(HomeworksAction.UpdateHomeworks(homeworksMap)))
                    if (scrollDate != null && !isScrolled) {
                        emit(EffectResult(HomeworksEffect.ScrollToDate(scrollDate)))
                        isScrolled = true
                    }
                },
            )
        }.onStart {
            emit(ActionResult(HomeworksAction.UpdateLoading(true)))
        }

        private fun loadActiveScheduleWork(currentDate: Instant) = flow<HomeworksWorkResult> {
            scheduleInteractor.fetchScheduleByDate(currentDate).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { schedule ->
                    emit(ActionResult(HomeworksAction.UpdateActiveSchedule(schedule.mapToUi())))
                },
            )
        }

        private fun loadUserPaidStatusWork() = flow {
            usersInteractor.fetchAppUserPaidStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(HomeworksAction.UpdateUserPaidStatus(it))) },
            )
        }

        private fun loadFriendsWork() = flow {
            usersInteractor.fetchAllFriends().collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { friends ->
                    emit(ActionResult(HomeworksAction.UpdateFriends(friends.map { it.mapToUi() })))
                },
            )
        }

        private fun updateHomeworkWork(homework: HomeworkDetailsUi) = flow {
            homeworksInteractor.updateHomework(homework.convertToBase().mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
            )
        }

        private fun shareHomeworksWork(sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) = flow {
            shareInteractor.shareHomeworks(sentMediatedHomeworks.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) }
            )
        }

        private fun scheduleGoalWork(goalCreateModel: GoalCreateModelUi) = flow {
            goalsInteractor.addGoal(goalCreateModel.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) }
            )
        }

        private fun deleteGoalWork(goal: GoalShortUi) = flow {
            goalsInteractor.deleteGoal(goal.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) }
            )
        }
    }
}

internal sealed class HomeworksDetailsWorkCommand : WorkCommand {
    data class LoadHomeworks(val timeRange: TimeRange, val scrollDate: Instant? = null) : HomeworksDetailsWorkCommand()
    data class LoadActiveSchedule(val currentDate: Instant) : HomeworksDetailsWorkCommand()
    data object LoadPaidUserStatus : HomeworksDetailsWorkCommand()
    data object LoadFriends : HomeworksDetailsWorkCommand()
    data class UpdateHomework(val homework: HomeworkDetailsUi) : HomeworksDetailsWorkCommand()
    data class ShareHomeworks(val sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) : HomeworksDetailsWorkCommand()
    data class ScheduleGoal(val goalCreateModel: GoalCreateModelUi) : HomeworksDetailsWorkCommand()
    data class DeleteGoal(val goal: GoalShortUi) : HomeworksDetailsWorkCommand()
}

internal typealias HomeworksWorkResult = WorkResult<HomeworksAction, HomeworksEffect, HomeworksOutput>