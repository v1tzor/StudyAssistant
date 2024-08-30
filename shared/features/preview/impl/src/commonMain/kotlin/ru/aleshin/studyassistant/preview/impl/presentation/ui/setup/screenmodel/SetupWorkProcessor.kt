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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.File
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.preview.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface SetupWorkProcessor :
    FlowWorkProcessor<SetupWorkCommand, SetupAction, SetupEffect> {

    class Base(
        private val appUserInteractor: AppUserInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
        private val calendarSettingsInteractor: CalendarSettingsInteractor,
    ) : SetupWorkProcessor {

        override suspend fun work(command: SetupWorkCommand) = when (command) {
            is SetupWorkCommand.LoadAllData -> fetchAllDataWork()
            is SetupWorkCommand.UpdateUserProfile -> updateProfileWork(
                user = command.user,
                actionWithAvatar = command.actionWithAvatar,
            )
            is SetupWorkCommand.UpdateOrganization -> updateOrganizationWork(
                organization = command.organization,
                actionWithAvatar = command.actionWithAvatar,
            )
            is SetupWorkCommand.UpdateCalendarSettings -> updateCalendarSettingsWork(
                settings = command.settings,
            )
        }

        private fun fetchAllDataWork() = flow {
            appUserInteractor.fetchAppUser().firstHandleAndGet(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = { user ->
                    val createdUser = user.mapToUi()
                    val mainOrganization = organizationsInteractor.fetchAllOrganization().firstOrNullHandleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { organizations ->
                            val mainOrganization = organizations.find { it.isMain }
                            val createdOrganization = mainOrganization ?: organizations.getOrNull(0)
                            return@firstOrNullHandleAndGet createdOrganization?.mapToUi()?.copy(isMain = true)
                        }
                    )
                    val calendarSettings = calendarSettingsInteractor.fetchCalendarSettings().firstHandleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { calendarSettings -> calendarSettings.mapToUi() },
                    )
                    val action = SetupAction.UpdateAll(
                        profile = createdUser,
                        organization = mainOrganization ?: OrganizationUi.createMainOrganization(),
                        calendarSettings = calendarSettings ?: CalendarSettingsUi.createEmpty(),
                    )
                    emit(ActionResult(action))
                },
            )
        }

        private fun updateProfileWork(
            user: AppUserUi,
            actionWithAvatar: ActionWithAvatar,
        ) = flow {
            val avatar = when (actionWithAvatar) {
                is ActionWithAvatar.Set -> {
                    appUserInteractor.uploadAvatar(File(actionWithAvatar.uri)).handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { it },
                    )
                }
                is ActionWithAvatar.Delete -> {
                    appUserInteractor.deleteAvatar().handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { null },
                    )
                }
                is ActionWithAvatar.None -> actionWithAvatar.uri
            }

            val updatedUser = user.copy(avatar = avatar)

            appUserInteractor.updateUser(updatedUser.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = {
                    val avatarAction = ActionWithAvatar.None(avatar)
                    emit(ActionResult(SetupAction.UpdateActionWithProfileAvatar(avatarAction)))
                    emit(ActionResult(SetupAction.UpdateUserProfile(updatedUser)))
                },
            )
        }

        private fun updateOrganizationWork(
            organization: OrganizationUi,
            actionWithAvatar: ActionWithAvatar,
        ) = flow {
            val uid = organization.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            val avatar = when (actionWithAvatar) {
                is ActionWithAvatar.Set -> {
                    organizationsInteractor.uploadAvatar(uid, File(actionWithAvatar.uri)).handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { it },
                    )
                }
                is ActionWithAvatar.Delete -> {
                    organizationsInteractor.deleteAvatar(uid).handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { null },
                    )
                }
                is ActionWithAvatar.None -> actionWithAvatar.uri
            }

            val updatedOrganization = organization.copy(uid = uid, avatar = avatar)

            organizationsInteractor.addOrUpdateOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = {
                    val avatarAction = ActionWithAvatar.None(avatar)
                    emit(ActionResult(SetupAction.UpdateActionWithOrganizationAvatar(avatarAction)))
                    emit(ActionResult(SetupAction.UpdateOrganization(updatedOrganization)))
                },
            )
        }

        private fun updateCalendarSettingsWork(settings: CalendarSettingsUi) = flow {
            calendarSettingsInteractor.updateCalendarSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(SetupAction.UpdateCalendarSettings(settings))) },
            )
        }
    }
}

internal sealed class SetupWorkCommand : WorkCommand {
    data object LoadAllData : SetupWorkCommand()
    data class UpdateUserProfile(val user: AppUserUi, val actionWithAvatar: ActionWithAvatar) : SetupWorkCommand()
    data class UpdateOrganization(
        val organization: OrganizationUi,
        val actionWithAvatar: ActionWithAvatar
    ) : SetupWorkCommand()
    data class UpdateCalendarSettings(val settings: CalendarSettingsUi) : SetupWorkCommand()
}