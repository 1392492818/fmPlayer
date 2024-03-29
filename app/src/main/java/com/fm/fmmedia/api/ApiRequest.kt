package com.fm.fmmedia.api

import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.request.Register
import com.fm.fmmedia.api.request.RegisterEmail
import com.fm.fmmedia.api.request.ShortVideo
import com.fm.fmmedia.api.response.Result
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE


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
    ): Result

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
    suspend fun memberInfo(@Header("Authorization") accessToken: String): Result;

    @Multipart
    @POST("video-file/video")
    suspend fun uploadVideoFile(
        @Header("Authorization") accessToken: String,
        @Part video: MultipartBody.Part
    ): Result;

    @Multipart
    @POST("file/image")
    suspend fun uploadImageFile(
        @Header("Authorization") accessToken: String,
        @Part image: MultipartBody.Part
    ): Result;

    @POST("shortVideo")
    suspend fun addShortVideo(
        @Header("Authorization") accessToken: String,
        @Body shortVideo: ShortVideo
    ): Result

    @GET("shortVideo")
    suspend fun getShortVideo(
        @Header("Authorization") accessToken: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String,
        @Query("sort") sort: String
    ): Result


    @GET("shortVideo/all")
    suspend fun getAllShortVideo(
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String,
        @Query("sort") sort: String
    ): Result
    @DELETE("shortVideo/{id}")
    suspend fun deleteShortVideo(
        @Header("Authorization") accessToken: String,
        @Path("id") id: Int
    ): Result

    @POST("member/register")
    suspend fun register(@Body register: Register): Result

    @POST("verification/registerEmail")
    suspend fun registerEmail(@Body email: RegisterEmail): Result

    @GET("member/publish")
    suspend fun memberPublish( @Header("Authorization") accessToken: String,
    ):Result

    @GET("live")
    suspend fun live(
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("search") search: String,
        @Query("sort") sort: String
    ): Result


    companion object {
        private const val BASE_URL = BuildConfig.API_BASE_URL
        fun create(): ApiRequest {
            val logger =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

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