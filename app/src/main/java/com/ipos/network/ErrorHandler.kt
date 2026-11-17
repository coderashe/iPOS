package com.ipos.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandler(private val gson: Gson) {

    /**
     * Parses error from HTTP response
     */
    fun parseError(response: Response<*>): Throwable {
        return try {
            val errorBody = response.errorBody()?.string() ?: ""
            val errorObject = try {
                JsonParser.parseString(errorBody).asJsonObject
            } catch (e: Exception) {
                JsonObject().apply {
                    addProperty("message", "Unknown error occurred")
                }
            }

            val errorMessage = when {
                errorObject.has("message") -> errorObject.get("message").asString
                errorObject.has("error") -> errorObject.get("error").asString
                else -> "Error ${response.code()}: ${response.message()}"
            }

            ApiException(
                code = response.code(),
                message = errorMessage,
                errorBody = errorObject
            )
        } catch (e: Exception) {
            ApiException(
                code = response.code(),
                message = response.message(),
                errorBody = null
            )
        }
    }

    /**
     * Traces and converts exceptions to user-friendly messages
     */
    fun traceErrorException(throwable: Throwable): Throwable {
        return when (throwable) {
            is HttpException -> {
                parseError(throwable.response()!!)
            }

            is SocketTimeoutException -> {
                ApiException(
                    message = "Connection timeout. Please check your internet connection",
                    cause = throwable
                )
            }

            is UnknownHostException -> {
                ApiException(
                    message = "No internet connection. Please check your network settings",
                    cause = throwable
                )
            }

            is IOException -> {
                ApiException(
                    message = "Network error occurred. Please try again",
                    cause = throwable
                )
            }

            is ApiException -> throwable // Already handled
            else -> {
                ApiException(
                    message = "An unexpected error occurred: ${throwable.message}",
                    cause = throwable
                )
            }
        }
    }

    /**
     * Custom API exception class
     */
    data class ApiException(
        val code: Int = -1,
        override val message: String,
        val errorBody: JsonObject? = null,
        override val cause: Throwable? = null
    ) : Throwable(message, cause)
}
