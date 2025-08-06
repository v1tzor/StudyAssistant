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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.screenmodel

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ShareSchedulesInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.covertToBase
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.prepareLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.BaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.MediatedBaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.convertToBase
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ReceivedMediatedSchedulesUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
internal interface ShareWorkProcessor :
    FlowWorkProcessor<ShareWorkCommand, ShareAction, ShareEffect> {

    class Base(
        private val shareSchedulesInteractor: ShareSchedulesInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
        private val schedulesInteractor: ScheduleInteractor,
    ) : ShareWorkProcessor {

        override suspend fun work(command: ShareWorkCommand) = when (command) {
            is ShareWorkCommand.LoadSharedSchedules -> loadSharedSchedules(
                shareId = command.shareId,
            )
            is ShareWorkCommand.LoadAllOrganizations -> loadAllOrganizations()
            is ShareWorkCommand.LinkOrganization -> linkOrganization(
                allLinkData = command.allLinkData,
                sharedSchedules = command.sharedSchedules,
                sharedOrganization = command.sharedOrganization,
                targetOrganization = command.targetOrganization
            )
            is ShareWorkCommand.UpdateLinkedSubjects -> updateLinkedSubjectsWork(
                allLinkData = command.allLinkData,
                sharedSchedules = command.sharedSchedules,
                sharedOrganization = command.sharedOrganization,
                subjects = command.subjects,
            )
            is ShareWorkCommand.UpdateLinkedEmployees -> updateLinkedEmployeesWork(
                allLinkData = command.allLinkData,
                sharedSchedules = command.sharedSchedules,
                sharedOrganization = command.sharedOrganization,
                teachers = command.teachers,
            )
            is ShareWorkCommand.RejectSharedSchedule -> rejectSharedScheduleWork(
                sharedSchedule = command.sharedSchedule,
            )
            is ShareWorkCommand.AcceptSharedSchedule -> acceptSharedScheduleWork(
                sharedSchedule = command.sharedSchedules,
                organizationsLinkData = command.organizationsLinkData,
                linkedSchedules = command.linkedSchedules,
            )
        }

        private fun loadSharedSchedules(shareId: UID) = flow<ShareWorkResult> {
            shareSchedulesInteractor.fetchReceivedSharedSchedules(shareId).first().handle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = { receivedSharedSchedules ->
                    val sharedSchedules = receivedSharedSchedules.mapToUi()
                    val linkData = sharedSchedules.organizationsData.map { it.prepareLinkData() }
                    val linkedSchedules = sharedSchedules.schedules.map { mediatedSchedule ->
                        mediatedSchedule.convertToBase(
                            linkDataMapper = { organizationId ->
                                checkNotNull(linkData.find { it.sharedOrganization.uid == organizationId })
                            }
                        )
                    }
                    val action = ShareAction.SetupSharedSchedules(
                        receivedMediatedSchedule = sharedSchedules,
                        organizationsLinkData = linkData,
                        linkedSchedules = linkedSchedules,
                    )
                    emit(ActionResult(action))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ShareAction.UpdateLoading(false)))
        }

        private fun loadAllOrganizations() = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(ShareAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun linkOrganization(
            allLinkData: List<OrganizationLinkData>,
            sharedSchedules: List<MediatedBaseScheduleUi>,
            sharedOrganization: UID,
            targetOrganization: UID?,
        ) = flow<ShareWorkResult> {
            val organizationData = targetOrganization?.let { targetOrganizationId ->
                organizationsInteractor.fetchOrganizationById(targetOrganizationId).firstHandleAndGet(
                    onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))).let { null } },
                    onRightAction = { it.mapToUi() },
                )
            }
            val updatedLinkData = allLinkData.map { linkData ->
                if (linkData.sharedOrganization.uid == sharedOrganization) {
                    return@map linkData.copy(
                        linkedOrganization = organizationData,
                        linkedSubjects = buildMap {
                            val targetSubjects = organizationData?.subjects ?: emptyList()
                            linkData.sharedOrganization.subjects.forEach { sharedSubject ->
                                val matchingSubject = targetSubjects.find { it.name.contains(sharedSubject.name) }
                                if (matchingSubject != null) put(sharedSubject.uid, matchingSubject)
                            }
                        },
                        linkedTeachers = buildMap {
                            val targetTeachers = organizationData?.employee ?: emptyList()
                            linkData.sharedOrganization.employee.forEach { sharedEmployee ->
                                val matchingSubject = targetTeachers.findLast {
                                    val firstNameMatching = it.firstName.contains(sharedEmployee.firstName)
                                    val secondNameMatching = it.secondName?.contains(sharedEmployee.secondName ?: "-")
                                    val patronymicNameMatching = it.patronymic?.contains(
                                        sharedEmployee.patronymic ?: "-"
                                    )
                                    return@findLast firstNameMatching && (secondNameMatching == true || patronymicNameMatching == true)
                                }
                                if (matchingSubject != null) put(sharedEmployee.uid, matchingSubject)
                            }
                        }
                    )
                } else {
                    return@map linkData
                }
            }
            val updatedLinkedSchedules = sharedSchedules.map { mediatedSchedule ->
                mediatedSchedule.convertToBase { organizationId ->
                    checkNotNull(updatedLinkData.find { it.sharedOrganization.uid == organizationId })
                }
            }
            emit(ActionResult(ShareAction.UpdateLinkData(updatedLinkData, updatedLinkedSchedules)))
        }.onStart {
            emit(ActionResult(ShareAction.UpdateLoadingLinkedOrganization(true)))
        }.onCompletion {
            emit(ActionResult(ShareAction.UpdateLoadingLinkedOrganization(false)))
        }

        private fun updateLinkedSubjectsWork(
            allLinkData: List<OrganizationLinkData>,
            sharedSchedules: List<MediatedBaseScheduleUi>,
            sharedOrganization: UID,
            subjects: Map<UID, SubjectUi>,
        ) = flow {
            val updatedLinkData = allLinkData.map { linkData ->
                if (linkData.sharedOrganization.uid == sharedOrganization) {
                    return@map linkData.copy(linkedSubjects = subjects)
                } else {
                    return@map linkData
                }
            }
            val updatedLinkedSchedules = sharedSchedules.map { mediatedSchedule ->
                mediatedSchedule.convertToBase { organizationId ->
                    checkNotNull(updatedLinkData.find { it.sharedOrganization.uid == organizationId })
                }
            }
            emit(ActionResult(ShareAction.UpdateLinkData(updatedLinkData, updatedLinkedSchedules)))
        }

        private fun updateLinkedEmployeesWork(
            allLinkData: List<OrganizationLinkData>,
            sharedSchedules: List<MediatedBaseScheduleUi>,
            sharedOrganization: UID,
            teachers: Map<UID, EmployeeUi>,
        ) = flow {
            val updatedLinkData = allLinkData.map { linkData ->
                if (linkData.sharedOrganization.uid == sharedOrganization) {
                    linkData.copy(linkedTeachers = teachers)
                } else {
                    linkData
                }
            }
            val updatedLinkedSchedules = sharedSchedules.map { mediatedSchedule ->
                mediatedSchedule.convertToBase { organizationId ->
                    checkNotNull(updatedLinkData.find { it.sharedOrganization.uid == organizationId })
                }
            }
            emit(ActionResult(ShareAction.UpdateLinkData(updatedLinkData, updatedLinkedSchedules)))
        }

        private fun rejectSharedScheduleWork(sharedSchedule: ReceivedMediatedSchedulesUi) = flow {
            shareSchedulesInteractor.acceptOrRejectSchedules(sharedSchedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(ShareEffect.NavigateToBack)) },
            )
        }

        private fun acceptSharedScheduleWork(
            sharedSchedule: ReceivedMediatedSchedulesUi,
            organizationsLinkData: List<OrganizationLinkData>,
            linkedSchedules: List<BaseScheduleUi>
        ) = flow<ShareWorkResult> {
            val updatedSubjectIds = mutableMapOf<UID, UID>()
            val updatedTeacherIds = mutableMapOf<UID, UID>()
            val updatedOrganizationsIds = mutableMapOf<UID, UID>()

            val organizations = organizationsLinkData.map { linkData ->
                val baseSharedOrganization = linkData.sharedOrganization.covertToBase()
                val linkedOrganization = linkData.linkedOrganization

                if (linkedOrganization != null) {
                    val newTeachers = baseSharedOrganization.employee
                        .filter { teacher -> linkData.linkedTeachers.containsKey(teacher.uid).not() }
                        .map { employee ->
                            val newEmployeeId = randomUUID().apply {
                                updatedTeacherIds[employee.uid] = this
                            }
                            employee.copy(
                                uid = newEmployeeId,
                                organizationId = linkedOrganization.uid,
                            )
                        }

                    val newSubjects = baseSharedOrganization.subjects
                        .filter { subject -> linkData.linkedSubjects.containsKey(subject.uid).not() }
                        .map { subject ->
                            val updatedSubjectId = randomUUID().apply {
                                updatedSubjectIds[subject.uid] = this
                            }
                            subject.copy(
                                uid = updatedSubjectId,
                                organizationId = linkedOrganization.uid,
                                teacher = subject.teacher?.let { teacher ->
                                    teacher.copy(
                                        uid = updatedTeacherIds[teacher.uid] ?: teacher.uid,
                                        organizationId = linkedOrganization.uid,
                                    )
                                }
                            )
                        }

                    val newOffices = baseSharedOrganization.offices.filter { office ->
                        linkedOrganization.offices.contains(office).not()
                    }
                    val newLocations = baseSharedOrganization.locations.filter { location ->
                        linkedOrganization.locations.find { it.value == location.value } == null
                    }
                    val updatedLinkedOrganization = linkedOrganization.copy(
                        subjects = linkedOrganization.subjects + newSubjects,
                        employee = linkedOrganization.employee + newTeachers,
                        offices = linkedOrganization.offices + newOffices,
                        locations = linkedOrganization.locations + newLocations,
                    )
                    return@map updatedLinkedOrganization.mapToDomain()
                } else {
                    val newOrganizationId = randomUUID().apply {
                        updatedOrganizationsIds[baseSharedOrganization.uid] = this
                    }
                    val updatedEmployees = baseSharedOrganization.employee.map { employee ->
                        val newEmployeeId = randomUUID().apply {
                            updatedTeacherIds[employee.uid] = this
                        }
                        employee.copy(
                            uid = newEmployeeId,
                            organizationId = newOrganizationId,
                        )
                    }
                    val updatedSubjects = baseSharedOrganization.subjects.map { subject ->
                        val updatedSubjectId = randomUUID().apply {
                            updatedSubjectIds[subject.uid] = this
                        }
                        subject.copy(
                            uid = updatedSubjectId,
                            organizationId = newOrganizationId,
                            teacher = subject.teacher?.let { teacher ->
                                teacher.copy(
                                    uid = updatedTeacherIds[teacher.uid] ?: teacher.uid,
                                    organizationId = newOrganizationId,
                                )
                            }
                        )
                    }
                    val updatedOrganization = baseSharedOrganization.copy(
                        uid = newOrganizationId,
                        subjects = updatedSubjects,
                        employee = updatedEmployees,
                    )
                    return@map updatedOrganization.mapToDomain()
                }
            }
            val schedules = linkedSchedules.map { schedule ->
                val newScheduleId = randomUUID()
                val updatedClasses = schedule.classes.mapNotNull { clazz ->
                    val organization = organizations.find { organization ->
                        organization.uid == (updatedOrganizationsIds[clazz.organization.uid] ?: clazz.organization.uid)
                    }
                    clazz.mapToDomain().copy(
                        uid = randomUUID(),
                        scheduleId = newScheduleId,
                        organization = organization?.convertToShort() ?: return@mapNotNull null,
                        teacher = clazz.teacher?.let { teacher ->
                            organization.employee.find { organizationTeacher ->
                                organizationTeacher.uid == (updatedTeacherIds[teacher.uid] ?: teacher.uid)
                            }
                        },
                        subject = clazz.subject?.let { subject ->
                            organization.subjects.find { organizationSubject ->
                                organizationSubject.uid == (updatedSubjectIds[subject.uid] ?: subject.uid)
                            }
                        },
                    )
                }
                BaseSchedule(
                    uid = newScheduleId,
                    dateVersion = schedule.dateVersion.mapToDomain(),
                    dayOfWeek = schedule.dayOfWeek,
                    week = schedule.week,
                    classes = updatedClasses,
                )
            }
//            val schedules = linkedSchedules.map { it.mapToDomain() }
//            val organizations = organizationsLinkData.map { linkData ->
//                val baseSharedOrganization = linkData.sharedOrganization.covertToBase()
//                val linkedOrganization = linkData.linkedOrganization
//                if (linkedOrganization != null) {
//                    val newSubjects = baseSharedOrganization.subjects.filter { subject ->
//                        linkData.linkedSubjects.containsKey(subject.uid).not()
//                    }.map { subject ->
//                        subject.copy(organizationId = linkedOrganization.uid)
//                    }
//                    val newTeachers = baseSharedOrganization.employee.filter { teacher ->
//                        linkData.linkedTeachers.containsKey(teacher.uid).not()
//                    }.map { employee ->
//                        employee.copy(organizationId = linkedOrganization.uid)
//                    }
//                    val newOffices = baseSharedOrganization.offices.filter { office ->
//                        linkedOrganization.offices.contains(office).not()
//                    }
//                    val newLocations = baseSharedOrganization.locations.filter { location ->
//                        linkedOrganization.locations.find { it.value == location.value } == null
//                    }
//                    val updatedLinkedOrganization = linkedOrganization.copy(
//                        subjects = linkedOrganization.subjects + newSubjects,
//                        employee = linkedOrganization.employee + newTeachers,
//                        offices = linkedOrganization.offices + newOffices,
//                        locations = linkedOrganization.locations + newLocations,
//                    )
//                    return@map updatedLinkedOrganization.mapToDomain()
//                } else {
//                    return@map baseSharedOrganization.mapToDomain()
//                }
//            }
            organizationsInteractor.addOrUpdateOrganizationsData(organizations).handle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = {
                    schedulesInteractor.addBaseSchedules(schedules).handle(
                        onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                        onRightAction = {
                            shareSchedulesInteractor.acceptOrRejectSchedules(sharedSchedule.mapToDomain()).handle(
                                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                                onRightAction = { emit(EffectResult(ShareEffect.NavigateToBack)) },
                            )
                        },
                    )
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateLoadingAccept(true)))
        }.onStart {
            emit(ActionResult(ShareAction.UpdateLoadingAccept(false)))
        }
    }
}

internal sealed class ShareWorkCommand : WorkCommand {
    data class LoadSharedSchedules(val shareId: UID) : ShareWorkCommand()
    data object LoadAllOrganizations : ShareWorkCommand()
    data class LinkOrganization(
        val allLinkData: List<OrganizationLinkData>,
        val sharedSchedules: List<MediatedBaseScheduleUi>,
        val sharedOrganization: UID,
        val targetOrganization: UID?
    ) : ShareWorkCommand()

    data class UpdateLinkedSubjects(
        val allLinkData: List<OrganizationLinkData>,
        val sharedSchedules: List<MediatedBaseScheduleUi>,
        val sharedOrganization: UID,
        val subjects: Map<UID, SubjectUi>
    ) : ShareWorkCommand()

    data class UpdateLinkedEmployees(
        val allLinkData: List<OrganizationLinkData>,
        val sharedSchedules: List<MediatedBaseScheduleUi>,
        val sharedOrganization: UID,
        val teachers: Map<UID, EmployeeUi>
    ) : ShareWorkCommand()

    data class RejectSharedSchedule(val sharedSchedule: ReceivedMediatedSchedulesUi) : ShareWorkCommand()

    data class AcceptSharedSchedule(
        val sharedSchedules: ReceivedMediatedSchedulesUi,
        val organizationsLinkData: List<OrganizationLinkData>,
        val linkedSchedules: List<BaseScheduleUi>,
    ) : ShareWorkCommand()
}

internal typealias ShareWorkResult = WorkResult<ShareAction, ShareEffect>