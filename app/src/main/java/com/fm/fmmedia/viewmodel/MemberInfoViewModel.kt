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
import com.fm.fmmedia.api.response.MemberInfoResponse
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.response.VideoGroupResponse
import com.fm.fmmedia.data.Word
import com.fm.fmmedia.repository.MemberInfoRepository
import com.fm.fmmedia.repository.VideoCategoryRepository
import com.fm.fmmedia.repository.VideoGroupRepository
import com.fm.fmmedia.repository.WordRepository
import kotlinx.coroutines.launch
import retrofit2.http.Query

class MemberInfoViewModel(private val repository: MemberInfoRepository): BaseVideoModel(repository) {
    val memberInfo: LiveData<MemberInfoResponse?> = repository.memberInfo.asLiveData()
    fun memberInfo(accessToken:String){
        viewModelScope.launch {
            repository.memberInfo(accessToken)
        }
    }


}

