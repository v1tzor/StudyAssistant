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

package ru.aleshin.studyassistant.auth.impl.domain.common

import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteDataAuthException
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteException
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.handlers.ErrorHandler

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal interface AuthErrorHandler : ErrorHandler<AuthFailures> {

    class Base : AuthErrorHandler {
        override fun handle(throwable: Throwable): AuthFailures = when (throwable) {
            is AppwriteDataAuthException -> AuthFailures.AuthorizationError
            is AppwriteUserException -> AuthFailures.NotFoundUserInfoError
            is NullPointerException -> AuthFailures.NotFoundUserInfoError
            is AppwriteException -> when (throwable.type) {
                "user_password_mismatch" -> AuthFailures.AuthorizationError
                "password_recently_used" -> AuthFailures.PasswordRecentlyUsedError
                "password_personal_data" -> AuthFailures.PasswordPersonalDataError
                "user_phone_not_found" -> AuthFailures.PhoneNotFoundError
                "user_missing_id" -> AuthFailures.MissingIdFromProviderError
                "user_oauth2_bad_request" -> AuthFailures.OAuthBadRequestError
                "user_jwt_invalid" -> AuthFailures.JwtInvalidError
                "user_blocked" -> AuthFailures.UserBlockedError
                "user_invalid_token" -> AuthFailures.NotFoundUserInfoError
                "user_email_not_whitelisted" -> AuthFailures.EmailNotWhitelistedError
                "user_invalid_code" -> AuthFailures.InvalidCodeError
                "user_ip_not_whitelisted" -> AuthFailures.IpNotWhitelistedError
                "user_invalid_credentials" -> AuthFailures.CredentialsError
                "user_anonymous_console_prohibited" -> AuthFailures.AnonymousConsoleProhibitedError
                "user_session_already_exists" -> AuthFailures.SessionAlreadyExistsError
                "user_unauthorized" -> AuthFailures.AccessDeniedError
                "user_oauth2_unauthorized" -> AuthFailures.OAuthUnauthorizedError
                "team_invalid_secret" -> AuthFailures.TeamInvalidSecretError
                "team_invite_mismatch" -> AuthFailures.TeamInviteMismatchError
                "user_not_found" -> AuthFailures.NotFoundUserInfoError
                "user_session_not_found" -> AuthFailures.SessionNotFoundError
                "user_identity_not_found" -> AuthFailures.IdentityNotFoundError
                "team_not_found" -> AuthFailures.TeamNotFoundError
                "team_invite_not_found" -> AuthFailures.TeamInviteNotFoundError
                "team_membership_mismatch" -> AuthFailures.TeamMembershipMismatchError
                "membership_not_found" -> AuthFailures.MembershipNotFoundError
                "user_already_exists" -> AuthFailures.UserAlreadyExistsError
                "user_email_already_exists" -> AuthFailures.EmailAlreadyUsedError
                "user_phone_already_exists" -> AuthFailures.UserAlreadyExistsError
                "team_invite_already_exists" -> AuthFailures.TeamInviteAlreadyExistsError
                "team_already_exists" -> AuthFailures.TeamAlreadyExistsError
                "membership_already_confirmed" -> AuthFailures.MembershipAlreadyConfirmedError
                "user_password_reset_required" -> AuthFailures.PasswordResetRequiredError
                "user_oauth2_provider_error" -> AuthFailures.OAuthProviderError
                "user_count_exceeded" -> AuthFailures.UserCountExceededError
                "user_auth_method_unsupported" -> AuthFailures.AuthMethodUnsupportedError
                else -> AuthFailures.OtherError(throwable)
            }
            is IllegalArgumentException -> AuthFailures.CredentialsError
            else -> AuthFailures.OtherError(throwable)
        }
    }
}