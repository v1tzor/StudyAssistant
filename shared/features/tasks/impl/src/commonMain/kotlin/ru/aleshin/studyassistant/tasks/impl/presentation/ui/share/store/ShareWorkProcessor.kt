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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ShareHomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.SubjectsInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareOutput

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal interface ShareWorkProcessor :
    FlowWorkProcessor<ShareWorkCommand, ShareAction, ShareEffect, ShareOutput> {

    class Base(
        private val shareInteractor: ShareHomeworksInteractor,
        private val subjectsInteractor: SubjectsInteractor,
    ) : ShareWorkProcessor {

        override suspend fun work(command: ShareWorkCommand) = when (command) {
            is ShareWorkCommand.FetchShare -> fetchShareWork(command.code)
            is ShareWorkCommand.LoadSubjects -> loadSubjectsWork(command.organizationId)
            is ShareWorkCommand.AcceptHomework -> acceptHomeworkWork(
                command.code,
                command.share,
                command.linkDataList,
            )
        }

        private fun fetchShareWork(code: String) = flow<ShareWorkResult> {
            shareInteractor.fetchSharePreview(code).handle(
                onLeftAction = { failure ->
                    when (val error = (failure as? TasksFailures.OtherError)?.throwable) {
                        is ShareException.InvalidCode -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.INVALID)))
                        }
                        is ShareException.Expired -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.EXPIRED)))
                        }
                        is ShareException.Duplicate -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.DUPLICATE)))
                        }
                        is ShareException.RateLimit,
                        is ShareException.ShareLimit,
                        is ShareException.ItemLimit,
                        is ShareException.PayloadTooLarge -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.INPUT)))
                            emit(EffectResult(ShareEffect.ShowError(failure)))
                        }
                        else -> when (failure) {
                            TasksFailures.InternetError -> emit(
                                ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.OFFLINE))
                            )
                            else -> emit(
                                ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.ERROR))
                            )
                        }
                    }
                },
                onRightAction = { preview ->
                    emit(ActionResult(ShareAction.SetupShare(
                        share = preview.share.mapToUi(),
                        linkDataList = preview.links.map { link -> link.mapToUi() },
                        linkSchedule = preview.schedule.mapToUi(),
                    )))
                    emit(ActionResult(ShareAction.UpdateOrganizations(
                        preview.organizations.map { organization -> organization.mapToUi() },
                    )))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.LOADING)))
        }

        private fun loadSubjectsWork(organizationId: UID) = flow<ShareWorkResult> {
            subjectsInteractor.fetchSubjectsByOrganization(organizationId).collectAndHandle(
                onLeftAction = { failure ->
                    emit(EffectResult(ShareEffect.ShowError(failure)))
                },
                onRightAction = { list ->
                    emit(ActionResult(ShareAction.UpdateSubjects(list.map { it.mapToUi() })))
                },
            )
        }

        private fun acceptHomeworkWork(
            code: String,
            share: HomeworkShareUi,
            linkData: List<MediatedHomeworkLinkData>,
        ) = flow<ShareWorkResult> {
            shareInteractor.importShare(
                code = code,
                share = share.mapToDomain(),
                links = linkData.map { link -> link.mapToDomain() },
            ).handle(
                onLeftAction = { failure ->
                    when (val error = (failure as? TasksFailures.OtherError)?.throwable) {
                        is ShareException.InvalidCode -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.INVALID)))
                        }
                        is ShareException.Expired -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.EXPIRED)))
                        }
                        is ShareException.Duplicate -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.DUPLICATE)))
                        }
                        is ShareException.RateLimit,
                        is ShareException.ShareLimit,
                        is ShareException.ItemLimit,
                        is ShareException.PayloadTooLarge -> {
                            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.PREVIEW)))
                            emit(EffectResult(ShareEffect.ShowError(failure)))
                        }
                        else -> when (failure) {
                            TasksFailures.InternetError -> emit(
                                ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.OFFLINE))
                            )
                            else -> emit(
                                ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.ERROR))
                            )
                        }
                    }
                },
                onRightAction = {
                    emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.SUCCESS)))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateStatus(HomeworkShareStatus.IMPORTING)))
        }

    }
}

internal sealed class ShareWorkCommand : WorkCommand {
    data class FetchShare(val code: String) : ShareWorkCommand()
    data class LoadSubjects(val organizationId: UID) : ShareWorkCommand()
    data class AcceptHomework(
        val code: String,
        val share: HomeworkShareUi,
        val linkDataList: List<MediatedHomeworkLinkData>,
    ) : ShareWorkCommand()
}

internal typealias ShareWorkResult = WorkResult<ShareAction, ShareEffect, ShareOutput>
