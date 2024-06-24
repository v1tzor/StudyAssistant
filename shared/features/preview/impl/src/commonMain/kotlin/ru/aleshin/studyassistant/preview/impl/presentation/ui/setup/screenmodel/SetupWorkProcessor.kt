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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.UID
import functional.firstHandleAndGet
import functional.firstOrNullHandleAndGet
import functional.handle
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.preview.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.UsersInteractor
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
internal interface SetupWorkProcessor : FlowWorkProcessor<SetupWorkCommand, SetupAction, SetupEffect> {

    class Base(
        private val usersInteractor: UsersInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
        private val calendarSettingsInteractor: CalendarSettingsInteractor,
    ) : SetupWorkProcessor {

        override suspend fun work(command: SetupWorkCommand) = when (command) {
            is SetupWorkCommand.FetchAllData -> fetchAllDataWork(command.userId)
            is SetupWorkCommand.SaveProfileInfo -> saveProfileInfoWork(command.user)
            is SetupWorkCommand.SaveOrganizationInfo -> saveOrganizationInfoWork(command.organization)
            is SetupWorkCommand.SaveCalendarSettings -> saveCalendarSettingsWork(command.settings)
        }

        private fun fetchAllDataWork(userId: UID) = flow {
            val createdUser = usersInteractor.fetchUserById(userId).firstHandleAndGet(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                onRightAction = { user -> user?.mapToUi() },
            )
            val mainOrganization = organizationsInteractor.fetchAllOrganization().firstOrNullHandleAndGet(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                onRightAction = { organizations ->
                    val mainOrganization = organizations.find { it.isMain }
                    val createdOrganization = mainOrganization ?: organizations.getOrNull(0)
                    return@firstOrNullHandleAndGet createdOrganization?.mapToUi()
                }
            )
            val calendarSettings = calendarSettingsInteractor.fetchCalendarSettings().firstHandleAndGet(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                onRightAction = { calendarSettings -> calendarSettings.mapToUi() },
            )
            val action = SetupAction.UpdateAll(
                profile = createdUser ?: AppUserUi.createEmpty(userId),
                organization = mainOrganization ?: OrganizationUi.createMainOrganization(),
                calendarSettings = calendarSettings ?: CalendarSettingsUi.createEmpty(),
            )
            emit(ActionResult(action))
        }

        private fun saveProfileInfoWork(user: AppUserUi) = flow {
            usersInteractor.updateUser(user.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(SetupAction.UpdateProfileInfo(user))) },
            )
        }

        private fun saveOrganizationInfoWork(organization: OrganizationUi) = flow {
            organizationsInteractor.createOrUpdateOrganization(organization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = { uid ->
                    emit(ActionResult(SetupAction.UpdateOrganizationInfo(organization.copy(uid = uid))))
                },
            )
        }

        private fun saveCalendarSettingsWork(settings: CalendarSettingsUi) = flow {
            calendarSettingsInteractor.updateCalendarSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(SetupAction.UpdateCalendarSettings(settings))) },
            )
        }
    }
}

internal sealed class SetupWorkCommand : WorkCommand {
    data class FetchAllData(val userId: UID) : SetupWorkCommand()
    data class SaveOrganizationInfo(val organization: OrganizationUi) : SetupWorkCommand()
    data class SaveProfileInfo(val user: AppUserUi) : SetupWorkCommand()
    data class SaveCalendarSettings(val settings: CalendarSettingsUi) : SetupWorkCommand()
}