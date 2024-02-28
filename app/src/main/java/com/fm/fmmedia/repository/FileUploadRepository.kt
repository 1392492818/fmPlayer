package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.request.ProgressRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FileUploadRepository : BaseRepository() {
    val videoUploadStatus: MutableStateFlow<Boolean> =
        MutableStateFlow<Boolean>(false)

    val videoUploadPath: MutableStateFlow<String?> = MutableStateFlow<String?>(null)

    val imageUploadStatus: MutableStateFlow<Boolean> =
        MutableStateFlow<Boolean>(false)

    val imageUploadPath: MutableStateFlow<String?> = MutableStateFlow<String?>(null)

    val TAG = FileUploadRepository::class.simpleName

    fun resetParams(){
        videoUploadStatus.value = false
        imageUploadStatus.value =false
    }

    suspend fun videoUpload(path: String, accessToken: String, progressCallback: (progress:Int)->Unit = {}, onSuccess: (path:String)-> Unit = {}) {
        val file: File = File(path)
        if (!file.isFile) {
            return
        }
        try {
            videoUploadStatus.value = false
            reset()
//            val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val requestBody = ProgressRequestBody(file = file, progressCallback = { progress ->
                run {
                    progressCallback(progress)
                }
            })

            val body = MultipartBody.Part.createFormData("video", file.name, requestBody)
            val result = ApiRequest.create().uploadVideoFile(accessToken, body)
            if (result.code == 0) {
                videoUploadPath.value = result.parseData<String>()
                videoUploadPath.value?.let { Log.e(TAG, it) }
                Log.e(TAG, videoUploadStatus.value.toString())
                videoUploadStatus.value = true
                Log.e(TAG, videoUploadStatus.value.toString())
                videoUploadPath.value?.let { onSuccess(it) }
            } else {
                errorCode.value = result.code
            }
        } catch (e: Exception) {
            isRequestError.value = true
            e?.message?.let { Log.e("测试", it) }
        }
    }

    suspend fun imageUpload(path: String, accessToken: String) {
        val file: File = File(path)
        if (!file.isFile) {
            return
        }
        try {
            imageUploadStatus.value = false
            reset()
            val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
            val result = ApiRequest.create().uploadImageFile(accessToken, body)
            if (result.code == 0) {
                imageUploadPath.value = result.parseData<String>()
                imageUploadPath.value?.let { Log.e(TAG, it) }
                imageUploadStatus.value = true
            } else {
                errorCode.value = result.code
            }
        } catch (e: Exception) {
            isRequestError.value = true
            e?.message?.let { Log.e("测试", it) }
        }
    }
}