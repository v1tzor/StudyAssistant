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

import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AppwriteAuth

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
interface AuthRemoteDataSourceOld {

    suspend fun createUserWithEmail(email: String, password: String): FirebaseUser?

    suspend fun fetchCurrentUser(): FirebaseUser?

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser?

    suspend fun signInViaGoogle(idToken: String?): FirebaseUser?

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun sendVerifyEmail()

    suspend fun updatePassword(oldPassword: String, newPassword: String)

    suspend fun signOut()

    class Base(
        private val firebaseAuth: FirebaseAuth,
        private val firestore: FirebaseFirestore,
        private val appwriteAuth: AppwriteAuth,
    ) : AuthRemoteDataSourceOld {

        override suspend fun createUserWithEmail(email: String, password: String): FirebaseUser? {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
            return authResult.user
        }

        override suspend fun fetchCurrentUser(): FirebaseUser? {
            return firebaseAuth.currentUser
        }

        override suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
            appwriteAuth.signInWithEmail(email, password)
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
            return authResult.user
        }

        override suspend fun signInViaGoogle(idToken: String?): FirebaseUser? {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential)
            return authResult.user
        }

        override suspend fun sendPasswordResetEmail(email: String) {
            firebaseAuth.sendPasswordResetEmail(email)
        }

        override suspend fun sendVerifyEmail() {
            val currentUser = firebaseAuth.currentUser ?: throw AppwriteUserException()
            currentUser.sendEmailVerification()
        }

        override suspend fun updatePassword(oldPassword: String, newPassword: String) {
            val currentUser = firebaseAuth.currentUser ?: throw AppwriteUserException()
            val userEmail = checkNotNull(currentUser.email)
            val authCredential = EmailAuthProvider.credential(userEmail, oldPassword)
            currentUser.reauthenticate(authCredential)
            currentUser.updatePassword(newPassword)
        }

        override suspend fun signOut() {
            firebaseAuth.signOut()
        }
    }
}