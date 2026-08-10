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

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareInput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareState

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class ShareComposeStore(
    private val workProcessor: ShareWorkProcessor,
    stateCommunicator: StateCommunicator<ShareState>,
    effectCommunicator: EffectCommunicator<ShareEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<ShareState, ShareEvent, ShareAction, ShareEffect, ShareInput, ShareOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: ShareInput, isRestore: Boolean) {
        dispatchEvent(ShareEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<ShareState, ShareAction, ShareEffect, ShareOutput>.handleEvent(
        event: ShareEvent,
    ) {
        when (event) {
            is ShareEvent.Started -> {
                if (!event.isRestore && !event.input.code.isNullOrBlank()) {
                    sendAction(ShareAction.UpdateCode(event.input.code))
                    launchBackgroundWork(BackgroundKey.LOAD_SHARE) {
                        val command = ShareWorkCommand.FetchShare(event.input.code)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ShareEvent.UpdatedCode -> {
                sendAction(ShareAction.UpdateCode(event.code))
            }
            is ShareEvent.FetchShare -> with(state) {
                launchBackgroundWork(BackgroundKey.LOAD_SHARE) {
                    val command = ShareWorkCommand.FetchShare(code)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ScannedCode -> {
                sendAction(ShareAction.UpdateCode(event.code))
                launchBackgroundWork(BackgroundKey.LOAD_SHARE) {
                    val command = ShareWorkCommand.FetchShare(event.code)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.UpdateLinkData -> with(state()) {
                val updated = linkDataList.toMutableList().apply {
                    val index = indexOfFirst { it.homework.uid == event.linkData.homework.uid }
                    if (index != -1) set(index, event.linkData)
                }
                sendAction(ShareAction.UpdateLinkData(updated))
            }
            is ShareEvent.LoadLinkSubjects -> {
                launchBackgroundWork(BackgroundKey.LOAD_SUBJECTS) {
                    val command = ShareWorkCommand.LoadSubjects(event.organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.AcceptHomework -> with(state()) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = ShareWorkCommand.AcceptHomework(
                        code = code,
                        share = checkNotNull(share),
                        linkDataList = linkDataList,
                    )
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ShareEvent.ClickEditSubject -> {
                val config = EditorConfig.Subject(event.subjectId, event.organization)
                consumeOutput(ShareOutput.NavigateToSubjectEditor(config))
            }
            is ShareEvent.Reset -> {
                sendAction(ShareAction.Reset)
            }
            is ShareEvent.BackClick -> {
                consumeOutput(ShareOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(action: ShareAction, currentState: ShareState) = when (action) {
        is ShareAction.UpdateCode -> currentState.copy(
            code = action.code
        )
        is ShareAction.UpdateStatus -> currentState.copy(
            status = action.status
        )
        is ShareAction.SetupShare -> currentState.copy(
            status = HomeworkShareStatus.PREVIEW,
            share = action.share,
            linkDataList = action.linkDataList,
            linkSchedule = action.linkSchedule,
        )
        is ShareAction.UpdateLinkData -> currentState.copy(
            linkDataList = action.linkDataList
        )
        is ShareAction.UpdateSubjects -> currentState.copy(
            linkSubjects = action.subjects
        )
        is ShareAction.UpdateOrganizations -> currentState.copy(
            organizations = action.organizations
        )
        is ShareAction.Reset -> ShareState()
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SHARE,
        LOAD_SUBJECTS,
        HOMEWORK_ACTION,
    }

    class Factory(
        private val workProcessor: ShareWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ShareComposeStore, ShareState> {
        override fun create(savedState: ShareState) = ShareComposeStore(
            workProcessor = workProcessor,
            stateCommunicator = StateCommunicator.Default(savedState),
            effectCommunicator = EffectCommunicator.Default(),
            coroutineManager = coroutineManager,
        )
    }
}
