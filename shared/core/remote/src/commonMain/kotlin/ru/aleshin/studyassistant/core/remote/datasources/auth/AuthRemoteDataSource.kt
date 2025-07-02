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

package ru.aleshin.studyassistant.core.remote.datasources.auth

import ru.aleshin.studyassistant.core.remote.appwrite.auth.AppwriteAuth
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AuthUserPojo

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
interface AuthRemoteDataSource {

    suspend fun fetchCurrentUser(): AuthUserPojo?
    suspend fun createUserWithEmail(email: String, password: String): AuthUserPojo?
    suspend fun signInWithEmail(email: String, password: String): AuthUserPojo?
    suspend fun signInViaGoogle(idToken: String?): AuthUserPojo?
    suspend fun sendVerifyEmail()
    suspend fun sendPasswordRecoveryEmail(email: String)
    suspend fun recoveryPassword(password: String, secret: String)
    suspend fun updatePassword(oldPassword: String?, newPassword: String)
    suspend fun signOut()

    class Base(
        private val appwriteAuth: AppwriteAuth,
    ) : AuthRemoteDataSource {

        override suspend fun fetchCurrentUser(): AuthUserPojo? {
            return appwriteAuth.fetchCurrentUser()
        }

        override suspend fun createUserWithEmail(email: String, password: String): AuthUserPojo? {
            val authResult = appwriteAuth.createUserWithEmail(email, password)
            return authResult
        }

        override suspend fun signInWithEmail(email: String, password: String): AuthUserPojo? {
            val authResult = appwriteAuth.signInWithEmail(email, password)
            return authResult
        }

        override suspend fun signInViaGoogle(idToken: String?): AuthUserPojo? {
//            val credential = GoogleAuthProvider.credential(idToken, null)
//            val authResult = appwriteAuth.signInWithCredential(credential)
//            return authResult.user
            return null
        }

        override suspend fun sendPasswordRecoveryEmail(email: String) {
            appwriteAuth.sendPasswordRecoveryEmail(email)
        }

        override suspend fun recoveryPassword(password: String, secret: String) {
            appwriteAuth.updateRecoveredPassword(password = password, secret = secret)
        }

        override suspend fun sendVerifyEmail() {
            appwriteAuth.sendVerifyEmail()
        }

        override suspend fun updatePassword(oldPassword: String?, newPassword: String) {
            appwriteAuth.updatePassword(oldPassword, newPassword)
        }

        override suspend fun signOut() {
            appwriteAuth.signOut()
        }
    }
}