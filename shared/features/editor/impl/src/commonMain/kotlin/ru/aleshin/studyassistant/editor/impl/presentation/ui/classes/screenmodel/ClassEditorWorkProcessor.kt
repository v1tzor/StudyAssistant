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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.TimeRange
import functional.UID
import functional.collectAndHandle
import functional.handle
import functional.handleAndGet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.api.ui.mapToDomain
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.SubjectInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.EditClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.convertToEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorEffect

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface ClassEditorWorkProcessor :
    FlowWorkProcessor<ClassEditorWorkCommand, ClassEditorAction, ClassEditorEffect> {

    class Base(
        private val baseScheduleInteractor: BaseScheduleInteractor,
        private val customScheduleInteractor: CustomScheduleInteractor,
        private val baseClassInteractor: BaseClassInteractor,
        private val customClassInteractor: CustomClassInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val employeeInteractor: EmployeeInteractor,
        private val subjectsInteractor: SubjectInteractor,
    ) : ClassEditorWorkProcessor {

        override suspend fun work(command: ClassEditorWorkCommand) = when (command) {
            is ClassEditorWorkCommand.LoadEditModel -> loadEditModelWork(
                classId = command.classId,
                scheduleId = command.scheduleId,
                isCustom = command.isCustomSchedule,
                weekDay = command.weekDay,
            )
            is ClassEditorWorkCommand.SaveEditModel -> saveEditModelWork(
                editModel = command.editModel,
                isCustomSchedule = command.isCustomSchedule,
                weekDay = command.weekDay,
            )
            is ClassEditorWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is ClassEditorWorkCommand.LoadOrganizationData -> loadOrganizationDataWork(command.organization)
        }

        private fun loadEditModelWork(
            classId: UID?,
            scheduleId: UID?,
            isCustom: Boolean,
            weekDay: DayOfNumberedWeekUi
        ) = flow {
            if (scheduleId != null) {
                if (isCustom) {
                    customScheduleInteractor.fetchScheduleById(scheduleId).collectAndHandle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { customSchedule ->
                            val schedule = customSchedule?.mapToUi()
                            val classModel = schedule?.classes?.find { it.uid == classId }
                            val editModel = classModel?.convertToEditModel() ?: EditClassUi.createEditModel(
                                uid = classId,
                                scheduleId = scheduleId,
                            )
                            if (editModel.organization != null) {
                                emit(EffectResult(ClassEditorEffect.LoadOrganizationData(editModel.organization)))
                            }
                            val timeRanges = schedule?.classes?.map { it.timeRange } ?: emptyList()
                            val action = ClassEditorAction.SetupEditModel(editModel, weekDay, isCustom, timeRanges)
                            emit(ActionResult(action))
                        }
                    )
                } else {
                    baseScheduleInteractor.fetchScheduleById(scheduleId).collectAndHandle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { baseSchedule ->
                            val schedule = baseSchedule?.mapToUi()
                            val classModel = schedule?.classes?.find { it.uid == classId }
                            val editModel = classModel?.convertToEditModel() ?: EditClassUi.createEditModel(
                                uid = classId,
                                scheduleId = scheduleId,
                            )
                            if (editModel.organization != null) {
                                emit(EffectResult(ClassEditorEffect.LoadOrganizationData(editModel.organization)))
                            }
                            val timeRanges = schedule?.classes?.map { it.timeRange } ?: emptyList()
                            val action = ClassEditorAction.SetupEditModel(editModel, weekDay, isCustom, timeRanges)
                            emit(ActionResult(action))
                        }
                    )
                }
            } else {
                val editModel = EditClassUi.createEditModel(classId, scheduleId)
                val timeRanges = emptyList<TimeRange>()
                emit(ActionResult(ClassEditorAction.SetupEditModel(editModel, weekDay, isCustom, timeRanges)))
            }
        }

        private fun saveEditModelWork(
            editModel: EditClassUi,
            isCustomSchedule: Boolean,
            weekDay: DayOfNumberedWeekUi,
        ) = flow {
            val classModel = editModel.convertToBase().mapToDomain()
            if (isCustomSchedule) {
                if (classModel.uid.isEmpty()) {
                    customClassInteractor.addClass(classModel).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                } else {
                    customClassInteractor.updateClass(classModel).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                }
            } else {
                if (classModel.uid.isEmpty()) {
                    baseClassInteractor.addClass(classModel, weekDay.mapToDomain()).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                } else {
                    baseClassInteractor.updateClass(classModel).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                }
            }
        }

        private fun loadOrganizationsWork() = flow {
            organizationInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(ClassEditorAction.UpdateOrganizations(organizations)))
                }
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadOrganizationDataWork(organization: OrganizationShortUi?) = flow {
            val organizationId = organization?.uid ?: return@flow emit(
                value = ActionResult(ClassEditorAction.UpdateOrganizationData()),
            )

            subjectsInteractor.fetchAllSubjectsByOrganization(organizationId).flatMapLatest { subjectsEither ->
                subjectsEither.handleAndGet(
                    onLeftAction = { flowOf(EffectResult(ClassEditorEffect.ShowError(it))) },
                    onRightAction = { subjectList ->
                        employeeInteractor.fetchAllEmployeeByOrganization(organizationId).map { employeeEither ->
                            employeeEither.handleAndGet(
                                onLeftAction = { EffectResult(ClassEditorEffect.ShowError(it)) },
                                onRightAction = { employeeList ->
                                    val subjects = subjectList.map { it.mapToUi() }
                                    val employees = employeeList.map { it.mapToUi() }
                                    val locations = organization.locations
                                    val offices = organization.offices
                                    ActionResult(
                                        ClassEditorAction.UpdateOrganizationData(
                                            subjects = subjects,
                                            employees = employees,
                                            locations = locations,
                                            offices = offices,
                                        )
                                    )
                                },
                            )
                        }
                    },
                )
            }.collect { result ->
                emit(result)
            }
        }
    }
}

internal sealed class ClassEditorWorkCommand : WorkCommand {
    data class LoadEditModel(
        val classId: UID?,
        val scheduleId: UID?,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassEditorWorkCommand()

    data class SaveEditModel(
        val editModel: EditClassUi,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassEditorWorkCommand()

    data object LoadOrganizations : ClassEditorWorkCommand()
    data class LoadOrganizationData(val organization: OrganizationShortUi?) : ClassEditorWorkCommand()
}
