package com.example.mitra

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {

    private val client: OkHttpClient

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Set read timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout
            .build()
    }

    private fun collectDeviceData(): String {
        val deviceModel = Build.MODEL
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val density = metrics.density

        val deviceData = mapOf(
            "device_model" to deviceModel,
            "screen_width" to screenWidth,
            "screen_height" to screenHeight,
            "density" to density
        )

        return Gson().toJson(deviceData)
    }

    fun sendImageCompletionRequest(
        prompt: String,
        imagePath: String,
        callback: (Boolean, String, String, Int, Int) -> Unit
    ) {
        val deviceData = collectDeviceData()
        val file = File(imagePath)
        val fileBody = file.asRequestBody("image/png".toMediaTypeOrNull())
        val formData = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, fileBody)
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("device_data", deviceData)
            .build()
        Log.d("ApiClient", "Device Data: $deviceData")


        val request = Request.Builder() // Use this from okhttp3.Request
            .url("https://top-snake-concise.ngrok-free.app/image-completion")
            .post(formData)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "API request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        Log.d("ApiClient", "API response: $responseBody")

                        responseBody?.let { body ->
                            try {
                                val jsonObjectWrapper =
                                    Gson().fromJson(body, JsonObject::class.java)
                                val jsonResponseString =
                                    jsonObjectWrapper.getAsJsonPrimitive("response").asString
                                val cleanJsonResponseString = jsonResponseString.replace("```json\n", "")
                                    .replace("\n```", "").trim()
                                val jsonResponse =
                                    JsonParser.parseString(cleanJsonResponseString).asJsonObject

                                val showArrow = jsonResponse.get("show_arrow").asBoolean
                                val status = jsonResponse.get("status").asString
                                val coordinates =
                                    jsonResponse.getAsJsonObject("immediate_action_coordinate")
                                val xAxis = coordinates.get("x_axis").asInt
                                val yAxis = coordinates.get("y_axis").asInt
                                val rotation = jsonResponse.get("rotation").asString

                                Handler(Looper.getMainLooper()).post {
                                    callback(showArrow, status, rotation, xAxis, yAxis)
                                }
                            } catch (e: Exception) {
                                Log.e("ApiClient", "Parsing error: ${e.message}")
                            }
                        }
                    } else {
                        val errorBody = it.body?.string()
                        Log.e(
                            "ApiClient", "API request failed with status code: ${it.code}, error: $errorBody"
                        )
                    }
                }
            }
        })
        // Mock response for testing purposes
//        Handler(Looper.getMainLooper()).post {
//            val showArrow = true
//            val status = "Tap the Twitter icon to open it"
//            val rotation = "upsidedown"
//            val xAxis = 24
//           val yAxis = 1500
//
//            callback(showArrow, status, rotation, xAxis, yAxis)
//        }
    }
}
