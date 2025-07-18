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

import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.Constants.App.RECOVERY_PASSWORD_URL
import ru.aleshin.studyassistant.core.common.functional.Constants.App.VERIFY_EMAIL_URL
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AccountService
import ru.aleshin.studyassistant.core.remote.models.appwrite.AuthUserPojo
import ru.aleshin.studyassistant.core.remote.models.appwrite.SessionPojo

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
interface AuthRemoteDataSource {

    suspend fun fetchCurrentUser(): AuthUserPojo?
    suspend fun createUserWithEmail(email: String, password: String, name: String?): AuthUserPojo?
    suspend fun signInWithEmail(email: String, password: String): SessionPojo
    suspend fun sendVerifyEmail()
    suspend fun sendPasswordRecoveryEmail(email: String)
    suspend fun recoveryPassword(password: String, secret: String)
    suspend fun updatePassword(oldPassword: String?, newPassword: String)
    suspend fun signOut()

    class Base(
        private val account: AccountService,
    ) : AuthRemoteDataSource {

        override suspend fun fetchCurrentUser(): AuthUserPojo? {
            return account.getCurrentUser()
        }

        override suspend fun createUserWithEmail(email: String, password: String, name: String?): AuthUserPojo? {
            val authUser = account.create(
                userId = randomUUID(),
                email = email,
                password = password,
                name = name,
            )
            account.createEmailPasswordSession(email, password)

            return authUser
        }

        override suspend fun signInWithEmail(email: String, password: String): SessionPojo {
            return account.createEmailPasswordSession(email, password)
        }

        override suspend fun sendPasswordRecoveryEmail(email: String) {
            account.createRecovery(email, RECOVERY_PASSWORD_URL)
        }

        override suspend fun recoveryPassword(password: String, secret: String) {
            val userId = account.getCurrentUser()?.id ?: throw AppwriteUserException()
            account.updateRecovery(userId = userId, password = password, secret = secret)
        }

        override suspend fun sendVerifyEmail() {
            account.createVerification(VERIFY_EMAIL_URL)
        }

        override suspend fun updatePassword(oldPassword: String?, newPassword: String) {
            account.updatePassword(password = newPassword, oldPassword = oldPassword)
        }

        override suspend fun signOut() {
            account.deleteSession("current")
        }
    }
}