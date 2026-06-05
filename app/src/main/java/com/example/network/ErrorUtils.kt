package com.example.network

import org.json.JSONObject
import retrofit2.Response
import retrofit2.HttpException

object ErrorUtils {
    fun parseErrorMessageFromHttpException(httpException: HttpException): String? {
        return try {
            val errorBodyString = httpException.response()?.errorBody()?.string()
            if (errorBodyString != null) {
                val jsonObject = JSONObject(errorBodyString)
                if (jsonObject.has("message")) {
                    jsonObject.getString("message")
                } else if (jsonObject.has("error")) {
                    jsonObject.getString("error")
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun parseErrorMessage(response: Response<*>): String? {
        return try {
            val errorBodyString = response.errorBody()?.string()
            if (errorBodyString != null) {
                val jsonObject = JSONObject(errorBodyString)
                if (jsonObject.has("message")) {
                    jsonObject.getString("message")
                } else if (jsonObject.has("error")) {
                    jsonObject.getString("error")
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
