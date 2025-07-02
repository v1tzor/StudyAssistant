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

package ru.aleshin.studyassistant.core.remote.appwrite

import io.appwrite.services.Account
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.Constants.App.OPEN_APP_URL
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AppwriteAuth
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AuthUserPojo

/**
 * @author Stanislav Aleshin on 29.06.2025.
 */
class AppwriteAuthAndroid(
    private val account: Account,
) : AppwriteAuth {

    override suspend fun createUserWithEmail(
        email: String,
        password: String
    ): AuthUserPojo? = catchError {
        val user = account.create(
            userId = randomUUID(),
            email = email,
            password = password,
        )

        return@catchError user.convertToCommon()
    }

    override suspend fun fetchCurrentUser(): AuthUserPojo? {
        return try {
            account.get().convertToCommon()
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthUserPojo? = catchError {
        account.createEmailPasswordSession(email, password)
        val user = account.get()

        return@catchError user.convertToCommon()
    }

    override suspend fun signInViaGoogle(idToken: String?): AuthUserPojo? {
        TODO("Not yet implemented")
    }

    override suspend fun sendPasswordRecoveryEmail(email: String) = catchError<Unit> {
        account.createRecovery(email, OPEN_APP_URL)
    }

    override suspend fun updateVerification(secret: String) = catchError<Unit> {
        val user = account.get()
        account.updateVerification(user.id, secret)
    }

    override suspend fun sendVerifyEmail() = catchError<Unit> {
        account.createVerification(OPEN_APP_URL)
    }

    override suspend fun updateRecoveredPassword(secret: String, password: String) = catchError<Unit> {
        val user = account.get()
        account.updateRecovery(user.id, secret, password)
    }

    override suspend fun updatePassword(oldPassword: String?, newPassword: String) = catchError<Unit> {
        account.updateSession("current")
        account.updatePassword(password = newPassword, oldPassword = oldPassword)
    }

    override suspend fun reloadUser(): AuthUserPojo? {
        account.updateSession("current")
        return fetchCurrentUser()
    }

    override suspend fun signOut() = catchError<Unit> {
        account.deleteSession("current")
    }

    private suspend fun <T> catchError(work: suspend () -> T): T {
        return try {
            work()
        } catch (exception: io.appwrite.exceptions.AppwriteException) {
            throw AppwriteException(
                message = exception.message,
                code = exception.code,
                type = exception.type,
                response = exception.response,
            )
        } catch (e: Exception) {
            throw e
        }
    }
}