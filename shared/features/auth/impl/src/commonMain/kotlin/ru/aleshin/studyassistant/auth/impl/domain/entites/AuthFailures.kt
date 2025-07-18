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

package ru.aleshin.studyassistant.auth.impl.domain.entites

import ru.aleshin.studyassistant.core.common.functional.DomainFailures

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal sealed class AuthFailures : DomainFailures {
    /** User with given ID/email/etc. not found */
    data object NotFoundUserInfoError : AuthFailures()

    /** Incorrect credentials */
    data object AuthorizationError : AuthFailures()

    /** Invalid or malformed user credentials */
    data object CredentialsError : AuthFailures()

    /** Rate limit reached */
    data object TooManyRequestsError : AuthFailures()

    /** A user already exists with the given data */
    data object UserAlreadyExistsError : AuthFailures()

    /** Email already used in this project */
    data object EmailAlreadyUsedError : AuthFailures()

    /** Password was recently used */
    data object PasswordRecentlyUsedError : AuthFailures()

    /** Password includes personal data */
    data object PasswordPersonalDataError : AuthFailures()

    /** Phone number not linked to user */
    data object PhoneNotFoundError : AuthFailures()

    /** OAuth provider did not return ID */
    data object MissingIdFromProviderError : AuthFailures()

    /** OAuth2 rejected bad request */
    data object OAuthBadRequestError : AuthFailures()

    /** JWT token is invalid */
    data object JwtInvalidError : AuthFailures()

    /** User is blocked */
    data object UserBlockedError : AuthFailures()

    /** Email is not whitelisted */
    data object EmailNotWhitelistedError : AuthFailures()

    /** Invalid confirmation or login code */
    data object InvalidCodeError : AuthFailures()

    /** IP address is not whitelisted */
    data object IpNotWhitelistedError : AuthFailures()

    /** Anonymous users are not allowed */
    data object AnonymousConsoleProhibitedError : AuthFailures()

    /** Anonymous session already exists */
    data object SessionAlreadyExistsError : AuthFailures()

    /** OAuth2 provider rejected unauthorized request */
    data object OAuthUnauthorizedError : AuthFailures()

    /** Invalid team invitation secret */
    data object TeamInvalidSecretError : AuthFailures()

    /** Team invite mismatch */
    data object TeamInviteMismatchError : AuthFailures()

    /** Session not found */
    data object SessionNotFoundError : AuthFailures()

    /** OAuth2 identity not found */
    data object IdentityNotFoundError : AuthFailures()

    /** Team with given ID not found */
    data object TeamNotFoundError : AuthFailures()

    /** Team invitation not found */
    data object TeamInviteNotFoundError : AuthFailures()

    /** Membership ID does not belong to the team */
    data object TeamMembershipMismatchError : AuthFailures()

    /** Membership with ID not found */
    data object MembershipNotFoundError : AuthFailures()

    /** Team invite already sent or exists */
    data object TeamInviteAlreadyExistsError : AuthFailures()

    /** Team with given ID already exists */
    data object TeamAlreadyExistsError : AuthFailures()

    /** Membership already confirmed */
    data object MembershipAlreadyConfirmedError : AuthFailures()

    /** User must reset password before proceeding */
    data object PasswordResetRequiredError : AuthFailures()

    /** OAuth2 provider returned internal error */
    data object OAuthProviderError : AuthFailures()

    /** Project reached user limit */
    data object UserCountExceededError : AuthFailures()

    /** Auth method disabled or not supported */
    data object AuthMethodUnsupportedError : AuthFailures()

    /** User is not authorized */
    data object AccessDeniedError : AuthFailures()

    /** Fallback error with original throwable */
    data class OtherError(val throwable: Throwable) : AuthFailures()
}