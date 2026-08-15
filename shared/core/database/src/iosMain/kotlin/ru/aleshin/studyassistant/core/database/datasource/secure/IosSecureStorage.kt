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

@file:OptIn(ExperimentalForeignApi::class)

package ru.aleshin.studyassistant.core.database.datasource.secure

import cnames.structs.__CFData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class IosSecureStorage {

    init {
        delete(account = LEGACY_PERSONAL_KEY, serviceName = LEGACY_SERVICE)
        delete(account = LEGACY_INSTALLATION_TOKEN, serviceName = LEGACY_SERVICE)
    }

    fun read(account: String): String? = memScoped {
        withBaseQuery(account) { query ->
            CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

            val result = alloc<CFTypeRefVar>()
            if (SecItemCopyMatching(query, result.ptr) != errSecSuccess) return@withBaseQuery null
            val data = result.value?.reinterpret<__CFData>() ?: return@withBaseQuery null
            try {
                val size = CFDataGetLength(data).toInt()
                val pointer = CFDataGetBytePtr(data) ?: return@withBaseQuery null
                ByteArray(size) { index -> pointer[index].toByte() }.decodeToString()
            } finally {
                CFRelease(result.value)
            }
        }
    }

    fun write(account: String, value: String) {
        if (value.isBlank()) {
            delete(account)
            return
        }
        delete(account)
        val bytes = value.encodeToByteArray()
        val data = checkNotNull(
            bytes.usePinned { pinned ->
                CFDataCreate(null, pinned.addressOf(0).reinterpret(), bytes.size.toLong())
            },
        )
        try {
            withBaseQuery(account) { query ->
                CFDictionarySetValue(query, kSecValueData, data)
                CFDictionarySetValue(
                    query,
                    kSecAttrAccessible,
                    kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
                )
                check(SecItemAdd(query, null) == errSecSuccess)
            }
        } finally {
            CFRelease(data)
        }
    }

    fun delete(account: String) {
        delete(account = account, serviceName = SERVICE)
    }

    private fun delete(
        account: String,
        serviceName: String,
    ) {
        withBaseQuery(account = account, serviceName = serviceName) { query ->
            SecItemDelete(query)
        }
    }

    private inline fun <T> withBaseQuery(
        account: String,
        serviceName: String = SERVICE,
        block: (CFMutableDictionaryRef) -> T
    ): T {
        val query = checkNotNull(CFDictionaryCreateMutable(null, 0, null, null))
        val service = checkNotNull(
            CFStringCreateWithCString(null, serviceName, kCFStringEncodingUTF8),
        )
        val accountValue =
            checkNotNull(CFStringCreateWithCString(null, account, kCFStringEncodingUTF8))
        return try {
            CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(query, kSecAttrService, service)
            CFDictionarySetValue(query, kSecAttrAccount, accountValue)
            block(query)
        } finally {
            CFRelease(accountValue)
            CFRelease(service)
            CFRelease(query)
        }
    }

    private companion object {
        const val SERVICE = "ru.aleshin.studyassistant.installation"
        const val LEGACY_SERVICE = "ru.aleshin.studyassistant.ai"
        const val LEGACY_PERSONAL_KEY = "personal_key"
        const val LEGACY_INSTALLATION_TOKEN = "installation_token"
    }
}
