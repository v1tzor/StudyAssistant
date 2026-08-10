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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.OutputResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.firstOrNullHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.settings.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.settings.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToDomain
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.preview.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.preview.impl.domain.interactors.ProfileInteractor
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupOutput
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface SetupWorkProcessor :
    FlowWorkProcessor<SetupWorkCommand, SetupAction, SetupEffect, SetupOutput> {

    class Base(
        private val profileInteractor: ProfileInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
        private val generalSettingsInteractor: GeneralSettingsInteractor,
        private val calendarSettingsInteractor: CalendarSettingsInteractor,
    ) : SetupWorkProcessor {

        override suspend fun work(command: SetupWorkCommand) = when (command) {
            is SetupWorkCommand.LoadAllData -> fetchAllDataWork()
            is SetupWorkCommand.UpdateProfile -> updateProfileWork(
                profile = command.profile,
                actionWithAvatar = command.actionWithAvatar,
            )

            is SetupWorkCommand.UpdateOrganization -> updateOrganizationWork(
                organization = command.organization,
                actionWithAvatar = command.actionWithAvatar,
            )

            is SetupWorkCommand.UpdateCalendarSettings -> updateCalendarSettingsWork(
                settings = command.settings,
            )

            is SetupWorkCommand.FinishSetup -> finishSetupWork(command.destination)
        }

        private fun fetchAllDataWork() = flow {
            profileInteractor.fetchProfile().firstHandleAndGet(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = { profile ->
                    val createdProfile = profile.mapToUi()
                    val mainOrganization =
                        organizationsInteractor.fetchAllOrganization().firstOrNullHandleAndGet(
                            onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                            onRightAction = { organizations ->
                                val mainOrganization = organizations.find { it.isMain }
                                val createdOrganization =
                                    mainOrganization ?: organizations.getOrNull(0)
                                return@firstOrNullHandleAndGet createdOrganization?.mapToUi()
                                    ?.copy(isMain = true)
                            }
                        )
                    val calendarSettings =
                        calendarSettingsInteractor.fetchCalendarSettings().firstHandleAndGet(
                            onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                            onRightAction = { calendarSettings -> calendarSettings.mapToUi() },
                        )
                    val action = SetupAction.UpdateAll(
                        profile = createdProfile,
                        organization = mainOrganization ?: OrganizationUi.createMainOrganization(),
                        calendarSettings = calendarSettings ?: CalendarSettingsUi.createEmpty(),
                    )
                    emit(ActionResult(action))
                },
            )
        }

        private fun updateProfileWork(
            profile: ProfileUi,
            actionWithAvatar: ActionWithAvatar,
        ) = flow {
            val avatar = when (actionWithAvatar) {
                is ActionWithAvatar.Set -> {
                    profileInteractor.uploadAvatar(profile.avatar, actionWithAvatar.file.mapToDomain())
                        .handleAndGet(
                            onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                            onRightAction = { it },
                        )
                }

                is ActionWithAvatar.Delete -> {
                    profileInteractor.deleteAvatar(profile.avatar ?: "").handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { null },
                    )
                }

                is ActionWithAvatar.None -> actionWithAvatar.uri
            }

            val updatedProfile = profile.copy(avatar = avatar)

            profileInteractor.updateProfile(updatedProfile.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = {
                    val avatarAction = ActionWithAvatar.None(avatar)
                    emit(ActionResult(SetupAction.UpdateActionWithProfileAvatar(avatarAction)))
                    emit(ActionResult(SetupAction.UpdateUserProfile(updatedProfile)))
                    emit(ActionResult(SetupAction.UpdatePage(SetupPage.ORGANIZATION)))
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
                    organizationsInteractor.uploadAvatar(
                        organization.avatar,
                        actionWithAvatar.file.mapToDomain()
                    ).handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { it },
                    )
                }

                is ActionWithAvatar.Delete -> {
                    organizationsInteractor.deleteAvatar(organization.avatar ?: "").handleAndGet(
                        onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))).let { null } },
                        onRightAction = { null },
                    )
                }

                is ActionWithAvatar.None -> actionWithAvatar.uri
            }

            val updatedOrganization = organization.copy(uid = uid, avatar = avatar)

            organizationsInteractor.addOrUpdateOrganization(updatedOrganization.mapToDomain())
                .handle(
                    onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                    onRightAction = {
                        val avatarAction = ActionWithAvatar.None(avatar)
                        emit(
                            ActionResult(
                                SetupAction.UpdateActionWithOrganizationAvatar(
                                    avatarAction
                                )
                            )
                        )
                        emit(ActionResult(SetupAction.UpdateOrganization(updatedOrganization)))
                        emit(ActionResult(SetupAction.UpdatePage(SetupPage.CALENDAR)))
                    },
                )
        }

        private fun updateCalendarSettingsWork(settings: CalendarSettingsUi) = flow {
            calendarSettingsInteractor.updateCalendarSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = {
                    emit(ActionResult(SetupAction.UpdateCalendarSettings(settings)))
                    emit(ActionResult(SetupAction.UpdatePage(SetupPage.SCHEDULE)))
                },
            )
        }

        private fun finishSetupWork(destination: SetupDestination) = flow {
            generalSettingsInteractor.completeOnboarding().handle(
                onLeftAction = { emit(EffectResult(SetupEffect.ShowError(it))) },
                onRightAction = {
                    val output = when (destination) {
                        SetupDestination.APP -> SetupOutput.NavigateToApp
                        SetupDestination.WEEK_SCHEDULE -> SetupOutput.NavigateToWeekScheduleEditor
                    }
                    emit(OutputResult(output))
                },
            )
        }
    }
}

internal sealed class SetupWorkCommand : WorkCommand {
    data object LoadAllData : SetupWorkCommand()
    data class UpdateProfile(val profile: ProfileUi, val actionWithAvatar: ActionWithAvatar) :
        SetupWorkCommand()

    data class UpdateOrganization(
        val organization: OrganizationUi,
        val actionWithAvatar: ActionWithAvatar
    ) : SetupWorkCommand()

    data class UpdateCalendarSettings(val settings: CalendarSettingsUi) : SetupWorkCommand()
    data class FinishSetup(val destination: SetupDestination) : SetupWorkCommand()
}

internal enum class SetupDestination {
    APP,
    WEEK_SCHEDULE,
}
