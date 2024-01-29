package com.fm.fmmedia.api

import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.response.Result
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiRequest {

    @POST("member/login")
    suspend fun login(
        @Body login: Login
    ): Result

    @GET("videoGroup")
    suspend fun videoGroup(
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String,
        @Query("sort") sort: String
    ):Result

    @GET("videoGroup/{id}")
    suspend fun findIdVideoGroup(
        @Path("id") id: Int
    ): Result

    @GET("categoryVideo")
    suspend fun categoryVideo(
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String,
        @Query("sort") sort: String
    ): Result

    @GET("member/info")
    suspend fun memberInfo(@Header("Authorization") accessToken:String): Result;


    companion object {
        private const val BASE_URL = BuildConfig.API_BASE_URL

        fun create(): ApiRequest {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequest::class.java)
        }
    }
}