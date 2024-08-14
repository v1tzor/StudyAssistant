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

package ru.aleshin.studyassistant.application.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.di.MainDependenciesGraph
import ru.aleshin.studyassistant.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.presentation.services.RemoteMessageHandler
import ru.rustore.sdk.pushclient.messaging.model.RemoteMessage
import ru.rustore.sdk.pushclient.messaging.service.RuStoreMessagingService
import ru.rustore.sdk.universalpush.rustore.messaging.toUniversalRemoteMessage

/**
 * @author Stanislav Aleshin on 11.08.2024.
 */
class RuStoreMessagingService : RuStoreMessagingService() {

    private val remoteMessageHandler by lazy {
        MainDependenciesGraph.fetchDI().instance<RemoteMessageHandler>()
    }

    private val appUserInteractor by lazy {
        MainDependenciesGraph.fetchDI().instance<AppUserInteractor>()
    }

    private val deviceInfoProvider by lazy {
        MainDependenciesGraph.fetchDI().instance<DeviceInfoProvider>()
    }

    private val coroutineManager by lazy {
        MainDependenciesGraph.fetchDI().instance<CoroutineManager>()
    }

    private val serviceJob = Job()

    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        remoteMessageHandler.handleMessage(message.toUniversalRemoteMessage())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updatePushTokenWork(token)
    }

    private fun updatePushTokenWork(token: String) = coroutineManager.runOnBackground(serviceScope) {
        appUserInteractor.fetchAppUser().first().handle(
            onLeftAction = { error("Error get AppUser for update FCM token") },
            onRightAction = { appUser ->
                val deviceId = deviceInfoProvider.fetchDeviceId()
                val currentDeviceInfo = appUser?.devices?.find { it.deviceId == deviceId }
                if (appUser != null && currentDeviceInfo != null && currentDeviceInfo.pushServiceType == PushServiceType.FCM) {
                    val actualDeviceInfo = UserDevice(
                        platform = deviceInfoProvider.fetchDevicePlatform(),
                        deviceId = deviceId,
                        deviceName = deviceInfoProvider.fetchDeviceName(),
                        pushToken = token,
                        pushServiceType = PushServiceType.FCM,
                    )
                    val updatedUser = appUser.copy(
                        devices = buildList {
                            addAll(appUser.devices)
                            remove(currentDeviceInfo)
                            add(actualDeviceInfo)
                        }
                    )
                    appUserInteractor.updateUser(updatedUser)
                }
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}