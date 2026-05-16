package com.example.myapplication.data.repository

import com.example.myapplication.data.model.AddInventoryResponse
import com.example.myapplication.data.model.InventoryListResponse
import com.example.myapplication.data.remote.InventoryApi
import com.example.myapplication.domain.repository.InventoryRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class InventoryRepositoryImpl(
    private val api: InventoryApi
) : InventoryRepository {

    override suspend fun getInventory(): Result<InventoryListResponse> {
        return try {
            val response = api.getInventory()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorString = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorString != null) {
                        JSONObject(errorString).optString("message", "Failed to load inventory")
                    } else {
                        "Failed with code: ${response.code()}"
                    }
                } catch (e: Exception) {
                    "Failed with code: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error occurred"))
        }
    }

    override suspend fun addInventory(
        movementType: String,
        type: String,
        description: String,
        pic: String,
        picture: File
    ): Result<AddInventoryResponse> {
        return try {
            val textMediaType = "text/plain".toMediaTypeOrNull()
            val imageMediaType = "image/jpeg".toMediaTypeOrNull()
            val picturePart = MultipartBody.Part.createFormData(
                "picture",
                picture.name,
                picture.asRequestBody(imageMediaType)
            )

            val response = api.addInventory(
                movement = movementType.toRequestBody(textMediaType),
                type = type.toRequestBody(textMediaType),
                description = description.toRequestBody(textMediaType),
                pic = pic.toRequestBody(textMediaType),
                picture = picturePart
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorString = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorString != null) {
                        JSONObject(errorString).optString("message", "Failed to add inventory")
                    } else {
                        "Failed with code: ${response.code()}"
                    }
                } catch (e: Exception) {
                    "Failed with code: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error occurred"))
        }
    }
}
