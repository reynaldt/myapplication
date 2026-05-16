package com.example.myapplication.data.remote

import com.example.myapplication.data.model.AddInventoryResponse
import com.example.myapplication.data.model.InventoryListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface InventoryApi {
    @GET("inventory")
    suspend fun getInventory(): Response<InventoryListResponse>

    @Multipart
    @POST("addinvent")
    suspend fun addInventory(
        @Part("movement") movement: RequestBody,
        @Part("type") type: RequestBody,
        @Part("description") description: RequestBody,
        @Part("PIC") pic: RequestBody,
        @Part picture: MultipartBody.Part
    ): Response<AddInventoryResponse>
}
