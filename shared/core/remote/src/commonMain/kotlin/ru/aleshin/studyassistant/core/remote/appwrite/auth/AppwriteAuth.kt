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

package ru.aleshin.studyassistant.core.remote.appwrite.auth

/**
 * @author Stanislav Aleshin on 28.06.2025.
 */
interface AppwriteAuth {
    suspend fun createUserWithEmail(email: String, password: String): AuthUserPojo?
    suspend fun fetchCurrentUser(): AuthUserPojo?
    suspend fun signInWithEmail(email: String, password: String): AuthUserPojo?
    suspend fun signInViaGoogle(idToken: String?): AuthUserPojo?
    suspend fun sendPasswordRecoveryEmail(email: String)
    suspend fun sendVerifyEmail()
    suspend fun updateVerification(secret: String)
    suspend fun updateRecoveredPassword(secret: String, password: String)
    suspend fun updatePassword(oldPassword: String?, newPassword: String)
    suspend fun reloadUser(): AuthUserPojo?
    suspend fun signOut()
}