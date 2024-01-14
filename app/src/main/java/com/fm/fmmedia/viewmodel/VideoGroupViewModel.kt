package com.fm.fmmedia.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.response.LoginResponse
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.response.VideoGroupResponse
import com.fm.fmmedia.data.Word
import com.fm.fmmedia.repository.VideoCategoryRepository
import com.fm.fmmedia.repository.VideoGroupRepository
import com.fm.fmmedia.repository.WordRepository
import kotlinx.coroutines.launch
import retrofit2.http.Query

class VideoGroupViewModel(private val repository: VideoGroupRepository): ViewModel() {
    val videoGroupList: LiveData<List<VideoGroupResponse>> = repository.videoGroupList.asLiveData()
    val videoGroup: LiveData<VideoGroupResponse?> = repository.videoGroup.asLiveData()
    fun getVideoGroup(pageNum: Int = 1,
                       pageSize: Int = 10,
                       search: String = "",
                       sort: String = ""){
        viewModelScope.launch {
            repository.getData(pageNum, pageSize, search, sort)
        }
    }



    fun findIdGroup(id: Int) {
        viewModelScope.launch {
            Log.e("findIdGroupd", "执行几次")
            repository.findIdData(id)
        }
    }
}

