package com.fm.fmmedia.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.repository.FileUploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileUploadViewModel(private val fileUploadRepository: FileUploadRepository) :
    BaseVideoModel(fileUploadRepository) {

    val videoUploadStatus: LiveData<Boolean> = fileUploadRepository.videoUploadStatus.asLiveData()
    val videoUploadPath: LiveData<String?> = fileUploadRepository.videoUploadPath.asLiveData()
    val imageUploadStatus: LiveData<Boolean> = fileUploadRepository.imageUploadStatus.asLiveData()
    val imageUploadPath: LiveData<String?> = fileUploadRepository.imageUploadPath.asLiveData()


    fun reset(){
        fileUploadRepository.resetParams()
    }

    fun videoUpload(
        path: String,
        accessToken: String,
        progressCallback: (progress: Int) -> Unit = {},
        onSuccess: (path:String)->Unit = {}
    ) {
        viewModelScope.launch {
            fileUploadRepository.videoUpload(
                path = path,
                accessToken = accessToken,
                progressCallback = progressCallback,
                onSuccess = onSuccess
            )
        }
    }



    fun imageUpload(path: String, accessToken: String) {
        viewModelScope.launch {
            fileUploadRepository.imageUpload(path, accessToken)
        }
    }

    suspend fun saveBitmapAsFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, fileName)
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                file
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
}