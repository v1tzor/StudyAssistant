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

package ru.aleshin.studyassistant.core.remote.models.ai

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
@Serializable
class DeepSeekError(val error: Error) {

    @Serializable
    data class Error(
        val message: String? = null,
        val type: String? = null,
        val param: JsonObject? = null,
        val code: String? = null,
    )
}

sealed class DeepSeekException(
    val statusCode: Int,
    val headers: Headers,
    val error: DeepSeekError?,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException("${error?.error?.message ?: ""}\n$message", cause) {

    class BadRequestException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(400, headers, error, message)

    class UnauthorizedException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(401, headers, error, message)

    class InsufficientBalanceException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(402, headers, error, message)

    class PermissionDeniedException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(403, headers, error, message)

    class NotFoundException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(404, headers, error, message)

    class UnprocessableEntityException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(422, headers, error, message)

    class RateLimitException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?,
    ) : DeepSeekException(429, headers, error, message)

    class InternalServerException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(500, headers, error, message)

    class OverloadServerException(
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(500, headers, error, message)

    class UnexpectedStatusCodeException(
        statusCode: Int,
        headers: Headers,
        error: DeepSeekError?,
        message: String?
    ) : DeepSeekException(statusCode, headers, error, message)

    companion object {
        fun from(statusCode: Int, headers: Headers, error: DeepSeekError?, message: String): DeepSeekException =
            when (statusCode) {
                400 -> BadRequestException(headers, error, message)
                401 -> UnauthorizedException(headers, error, message)
                402 -> InsufficientBalanceException(headers, error, message)
                403 -> PermissionDeniedException(headers, error, message)
                404 -> NotFoundException(headers, error, message)
                422 -> UnprocessableEntityException(headers, error, message)
                429 -> RateLimitException(headers, error, message)
                500 -> InternalServerException(headers, error, message)
                else -> UnexpectedStatusCodeException(statusCode, headers, error, message)
            }

        fun from(statusCode: Int, headers: Headers, error: DeepSeekError?): DeepSeekException =
            when (statusCode) {
                400 -> BadRequestException(
                    headers,
                    error,
                    "Please modify your request body according to the hints in the error message.\nFor more API format details, please refer to [DeepSeek API Docs](https://api-docs.deepseek.com/)."
                )
                401 -> UnauthorizedException(
                    headers,
                    error,
                    "Please check your API key.\nIf you don't have one, please [create an API key](https://platform.deepseek.com/api_keys) first."
                )
                402 -> InsufficientBalanceException(
                    headers,
                    error,
                    "Please check your account's balance, and go to the [Top up](https://platform.deepseek.com/top_up) page to add funds."
                )
                403 -> PermissionDeniedException(
                    headers,
                    error,
                    "Please check your API key.\nIf you don't have one, please [create an API key](https://platform.deepseek.com/api_keys) first."
                )
                404 -> NotFoundException(
                    headers,
                    error,
                    "Please check the API endpoint you are using.\nFor more API format details, please refer to [DeepSeek API Docs](https://api-docs.deepseek.com/)."
                )
                422 -> UnprocessableEntityException(
                    headers,
                    error,
                    "Please modify your request parameters according to the hints in the error message.\nFor more API format details, please refer to [DeepSeek API Docs](https://api-docs.deepseek.com/)."
                )
                429 -> RateLimitException(
                    headers,
                    error,
                    "Please pace your requests reasonably.\nWe also advise users to temporarily switch to the APIs of alternative LLM service providers, like OpenAI."
                )
                500 -> InternalServerException(
                    headers,
                    error,
                    "Please retry your request after a brief wait and contact us if the issue persists."
                )
                503 -> OverloadServerException(headers, error, "Please retry your request after a brief wait.")
                else -> UnexpectedStatusCodeException(statusCode, headers, error, "Unexpected status code: $statusCode")
            }
    }
}

suspend inline fun <reified T> HttpResponse.bodyOrAiError(): T {
    return if (!status.isSuccess()) {
        val headers = headers
        val error = body<DeepSeekError>()
        val description = status.description
        if (description.isEmpty()) {
            throw DeepSeekException.from(status.value, headers, error)
        } else {
            throw DeepSeekException.from(status.value, headers, error, description)
        }
    } else {
        body<T>()
    }
}