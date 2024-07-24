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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.screenmodel

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.core.domain.entities.share.convertToBase
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ShareHomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.SubjectsInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.NumberedClassUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.createHomework
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.prepareDataForLink
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEffect

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal interface ShareWorkProcessor :
    FlowWorkProcessor<ShareWorkCommand, ShareAction, ShareEffect> {

    class Base(
        private val shareInteractor: ShareHomeworksInteractor,
        private val subjectsInteractor: SubjectsInteractor,
        private val homeworksInteractor: HomeworksInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val scheduleInteractor: ScheduleInteractor,
    ) : ShareWorkProcessor {

        override suspend fun work(command: ShareWorkCommand) = when (command) {
            is ShareWorkCommand.LoadSharedHomeworks -> loadSharedHomeworksWork()
            is ShareWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is ShareWorkCommand.LoadLinkData -> loadLinkData(command.receivedHomeworks)
            is ShareWorkCommand.LoadSubjects -> loadSubjectsWork(command.organizationId)
            is ShareWorkCommand.AcceptHomework -> acceptHomeworkWork(
                mediatedHomeworks = command.receivedHomeworks,
                linkData = command.linkDataList,
                organizations = command.organizations,
            )
            is ShareWorkCommand.CancelSendHomework -> cancelSendHomeworkWork(command.sentHomeworks)
            is ShareWorkCommand.RejectHomework -> rejectHomeworkWork(command.receivedHomeworks)
        }

        private fun loadSharedHomeworksWork() = flow {
            shareInteractor.fetchSharedHomeworksDetails().collectAndHandle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = { sharedHomeworks ->
                    emit(ActionResult(ShareAction.UpdateSharedHomeworks(sharedHomeworks.mapToUi())))
                },
            )
        }
        private fun loadOrganizationsWork() = flow {
            organizationInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(ShareAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun loadLinkData(receivedHomeworks: ReceivedMediatedHomeworksDetailsUi) = flow<ShareWorkResult> {
            val subjectNames = receivedHomeworks.homeworks.map { it.subjectName }
            val subjects = subjectsInteractor.fetchSubjectsByNames(subjectNames).handleAndGet(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))).let { null } },
                onRightAction = { subjects -> subjects.map { it.mapToUi() } },
            )
            val schedule = scheduleInteractor.fetchScheduleByDate(
                date = receivedHomeworks.date.startThisDay(),
            ).firstHandleAndGet(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))).let { null } },
                onRightAction = { it.mapToUi() },
            )

            val linkDataList = receivedHomeworks.homeworks.map { homework ->
                val actualLinkedClass = schedule?.classes?.find {
                    it.subject?.name == homework.subjectName
                }
                homework.prepareDataForLink(
                    actualSubject = subjects?.find { it.name == homework.subjectName },
                    actualLinkedClass = if (actualLinkedClass != null) {
                        NumberedClassUi(actualLinkedClass, schedule.classes.indexOf(actualLinkedClass).inc())
                    } else {
                        null
                    },
                )
            }

            emit(ActionResult(ShareAction.SetupLinkData(linkDataList, schedule)))
        }.onStart {
            emit(ActionResult(ShareAction.UpdateLinkLoading(true)))
        }

        private fun loadSubjectsWork(organizationId: UID) = flow {
            subjectsInteractor.fetchSubjectsByOrganization(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = { subjectList ->
                    val subjects = subjectList.map { it.mapToUi() }
                    emit(ActionResult(ShareAction.UpdateSubjects(subjects)))
                },
            )
        }

        private fun acceptHomeworkWork(
            mediatedHomeworks: ReceivedMediatedHomeworksDetailsUi,
            linkData: List<MediatedHomeworkLinkData>,
            organizations: List<OrganizationShortUi>,
        ) = flow {
            val homeworks = mediatedHomeworks.homeworks.map { mediatedHomework ->
                val homeworksLinkData = linkData.find { it.homework.uid == mediatedHomework.uid }
                val organizationId = homeworksLinkData?.actualSubject?.organizationId
                return@map mediatedHomework.createHomework(
                    date = mediatedHomeworks.date.startThisDay(),
                    organization = checkNotNull(organizations.find { it.uid == organizationId }),
                    linkData = checkNotNull(homeworksLinkData),
                ).mapToDomain()
            }
            homeworksInteractor.addHomeworksGroup(homeworks).handle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
                onRightAction = {
                    shareInteractor.acceptOrRejectHomeworks(mediatedHomeworks.mapToDomain().convertToBase())
                },
            )
        }

        private fun cancelSendHomeworkWork(mediatedHomeworks: SentMediatedHomeworksDetailsUi) = flow {
            shareInteractor.cancelSendHomeworks(mediatedHomeworks.mapToDomain().convertToBase()).handle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
            )
        }

        private fun rejectHomeworkWork(mediatedHomeworks: ReceivedMediatedHomeworksDetailsUi) = flow {
            shareInteractor.acceptOrRejectHomeworks(mediatedHomeworks.mapToDomain().convertToBase()).handle(
                onLeftAction = { emit(EffectResult(ShareEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class ShareWorkCommand : WorkCommand {

    data object LoadSharedHomeworks : ShareWorkCommand()

    data class LoadLinkData(val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi) : ShareWorkCommand()

    data object LoadOrganizations : ShareWorkCommand()

    data class LoadSubjects(val organizationId: UID) : ShareWorkCommand()

    data class AcceptHomework(
        val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi,
        val linkDataList: List<MediatedHomeworkLinkData>,
        val organizations: List<OrganizationShortUi>,
    ) : ShareWorkCommand()

    data class RejectHomework(
        val receivedHomeworks: ReceivedMediatedHomeworksDetailsUi,
    ) : ShareWorkCommand()

    data class CancelSendHomework(
        val sentHomeworks: SentMediatedHomeworksDetailsUi,
    ) : ShareWorkCommand()
}

internal typealias ShareWorkResult = WorkResult<ShareAction, ShareEffect>