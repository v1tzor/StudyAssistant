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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.ui.ads.RewardedAdSession
import ru.aleshin.studyassistant.core.ui.theme.tokens.CustomColors
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleImportHandler
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal class ImportComposeStore(
    private val workProcessor: ImportWorkProcessor,
    private val importHandler: ScheduleImportHandler,
    stateCommunicator: StateCommunicator<ImportState>,
    effectCommunicator: EffectCommunicator<ImportEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<ImportState, ImportEvent, ImportAction, ImportEffect, ImportInput, ImportOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    private var applyStarted = false

    override fun initialize(input: ImportInput, isRestore: Boolean) {
        dispatchEvent(ImportEvent.Started)
        if (isRestore) {
            dispatchEvent(ImportEvent.ReconcileReward)
        }
    }

    override suspend fun WorkScope<ImportState, ImportAction, ImportEffect, ImportOutput>.handleEvent(
        event: ImportEvent,
    ) {
        when (event) {
            is ImportEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_DATA) {
                    val command = ImportWorkCommand.LoadOrganizations
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.SelectedPhoto -> {
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    val command = ImportWorkCommand.PrepareImage(event.imageBytes)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.ImageSelectionFailed -> {
                sendEffect(ImportEffect.ShowError(ScheduleFailures.InvalidImage))
            }
            is ImportEvent.UpdateNote -> {
                sendAction(ImportAction.UpdateNote(event.note))
            }
            is ImportEvent.SelectOrganization -> {
                sendAction(ImportAction.UpdateSelectedOrganization(event.organization))
            }
            is ImportEvent.ExtractDraft -> with(state) {
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    val command = ImportWorkCommand.ExtractDraft(
                        note = note,
                        organizationId = selectedOrganization?.uid ?: return@launchBackgroundWork,
                        image = preparedImage ?: return@launchBackgroundWork
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.UpdateClass -> with(state) {
                val current = session ?: return
                val updated = importHandler.updateClass(current.mapToDomain(), event.classModel.mapToDomain())
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.UpdateSubject -> with(state) {
                val current = session ?: return
                val updated = importHandler.updateSubject(current.mapToDomain(), event.subject.mapToDomain())
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.UpdateEmployee -> with(state) {
                val current = session ?: return
                val updated = importHandler.updateEmployee(current.mapToDomain(), event.employee.mapToDomain())
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.AssignSubject -> with(state) {
                val current = session ?: return
                val updated = importHandler.assignSubject(current.mapToDomain(), event.classId, event.subjectId)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.AssignTeacher -> with(state) {
                val current = session ?: return
                val updated = importHandler.assignTeacher(current.mapToDomain(), event.classId, event.teacherId)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.AddSubject -> with(state) {
                val current = session ?: return
                val subject = Subject(
                    uid = event.uid,
                    organizationId = current.organizationId,
                    eventType = EventType.LESSON,
                    name = event.name,
                    teacher = null,
                    office = "",
                    color = CustomColors.randomUnusedArgb(current.subjects.map { subject -> subject.color }),
                    location = null,
                    updatedAt = 0L,
                )
                val updated = importHandler.addSubject(current.mapToDomain(), subject)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.AddEmployee -> with(state) {
                val current = session ?: return
                val employee = Employee(
                    uid = event.uid,
                    organizationId = current.organizationId,
                    firstName = event.firstName,
                    secondName = null,
                    patronymic = null,
                    post = EmployeePost.TEACHER,
                    updatedAt = 0L,
                )
                val updated = importHandler.addEmployee(current.mapToDomain(), employee)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.DeleteClass -> with(state) {
                val current = session ?: return
                val updated = importHandler.deleteClass(current.mapToDomain(), event.classId)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.DeleteSubject -> with(state) {
                val current = session ?: return
                val updated = importHandler.deleteSubject(current.mapToDomain(), event.subjectId)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.DeleteEmployee -> with(state) {
                val current = session ?: return
                val updated = importHandler.deleteEmployee(current.mapToDomain(), event.employeeId)
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.ReorderDayClasses -> with(state) {
                val current = session ?: return
                val updated = importHandler.reorderDayClasses(
                    session = current.mapToDomain(),
                    dayOfWeek = event.dayOfWeek,
                    repeatWeek = event.repeatWeek,
                    orderedIds = event.orderedIds,
                )
                sendAction(ImportAction.SetupSession(updated.mapToUi(), requestId))
            }
            is ImportEvent.ApplySession -> with(state) {
                if (session != null && selectedOrganization != null && requestId != null) {
                    launchBackgroundWork(BackgroundKey.REWARD) {
                        val command = ImportWorkCommand.PrepareImportReward(
                            requestId = requestId,
                            session = session,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                    }
                } else {
                    sendEffect(ImportEffect.ShowError(ScheduleFailures.InvalidImport))
                }
            }
            is ImportEvent.RewardedAdGranted -> with(state) {
                if (isApplied || applyStarted) return
                if (session != null) {
                    applyStarted = true
                    launchBackgroundWork(BackgroundKey.APPLY) {
                        val command = ImportWorkCommand.ApplySession(
                            session = session,
                            rewardChallengeId = event.challengeId,
                        )
                        workProcessor.work(command).collectAndHandleWork()
                        if (!state.isApplied) applyStarted = false
                    }
                } else {
                    sendAction(ImportAction.UpdateRewardChallenge(null, false))
                    sendEffect(ImportEffect.ShowError(ScheduleFailures.InvalidImport))
                }
            }
            is ImportEvent.RewardedAdUnavailable -> {
                sendAction(ImportAction.UpdateRewardChallenge(null, false))
                sendEffect(ImportEffect.ShowError(ScheduleFailures.RewardUnavailable))
            }
            is ImportEvent.ReconcileReward -> with(state) {
                val challengeId = rewardChallengeId
                when {
                    challengeId != null && RewardedAdSession.hasRewarded(challengeId) -> {
                        val currentSession = session
                        if (isApplied || applyStarted) {
                            return
                        }
                        if (currentSession != null) {
                            applyStarted = true
                            launchBackgroundWork(BackgroundKey.APPLY) {
                                val command = ImportWorkCommand.ApplySession(
                                    session = currentSession,
                                    rewardChallengeId = challengeId,
                                )
                                workProcessor.work(command).collectAndHandleWork()
                                if (!state.isApplied) applyStarted = false
                            }
                        } else {
                            sendAction(ImportAction.UpdateRewardChallenge(null, false))
                            sendEffect(ImportEffect.ShowError(ScheduleFailures.InvalidImport))
                        }
                    }
                    challengeId != null && RewardedAdSession.isPresented(challengeId) -> Unit
                    else -> sendAction(ImportAction.UpdateRewardChallenge(null, false))
                }
            }
            is ImportEvent.EditSource -> {
                sendAction(ImportAction.SetupSession(null, null))
            }
            is ImportEvent.ClickBack -> with(state) {
                if (session != null && !isApplied) {
                    sendAction(ImportAction.SetupSession(null, requestId))
                } else {
                    consumeOutput(ImportOutput.NavigateToBack)
                }
            }
            is ImportEvent.ClickAddOrganization -> {
                consumeOutput(ImportOutput.NavigateToOrganizationEditor(null))
            }
        }
    }

    override suspend fun reduce(action: ImportAction, currentState: ImportState) = when (action) {
        is ImportAction.UpdateLoadingPhoto -> currentState.copy(
            isLoadingPhoto = action.isLoading
        )
        is ImportAction.UpdateAnalysisProgress -> currentState.copy(
            isAnalysisInProgress = action.isLoading,
            analysisStartedAt = action.startedAt,
        )
        is ImportAction.SetupOrganizations -> currentState.copy(
            organizations = action.organizations
        )
        is ImportAction.UpdatePhoto -> currentState.copy(
            preparedImage = action.preparedImage
        )
        is ImportAction.UpdateNote -> currentState.copy(
            note = action.note
        )
        is ImportAction.UpdateSelectedOrganization -> currentState.copy(
            selectedOrganization = action.organization
        )
        is ImportAction.SetupSession -> currentState.copy(
            session = action.session,
            requestId = action.requestId,
        )
        is ImportAction.UpdateApplied -> currentState.copy(
            isApplied = action.isApplied
        )
        is ImportAction.UpdateRewardChallenge -> currentState.copy(
            rewardChallengeId = action.challengeId,
            isRewardInProgress = action.isInProgress
        )
    }

    private enum class BackgroundKey : BackgroundWorkKey {
        LOAD_DATA,
        PROCESS,
        REWARD,
        APPLY,
    }

    class Factory(
        private val workProcessor: ImportWorkProcessor,
        private val composer: ScheduleImportHandler,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ImportComposeStore, ImportState> {
        override fun create(savedState: ImportState) = ImportComposeStore(
            workProcessor = workProcessor,
            importHandler = composer,
            stateCommunicator = StateCommunicator.Default(savedState),
            effectCommunicator = EffectCommunicator.Default(),
            coroutineManager = coroutineManager,
        )
    }
}
