package com.fm.fmmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.api.request.ShortVideo
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.repository.ShortVideoRepository
import kotlinx.coroutines.launch

class ShortVideoViewModel(private val repository: ShortVideoRepository) :
    BaseVideoModel(repository) {

    val isAddSuccess = repository.isAddSuccess.asLiveData()
    val page:LiveData<Page?> = repository.page.asLiveData()

    fun addShortVideo(accessToken:String, shortVideo: ShortVideo){
        viewModelScope.launch {
            repository.addShortVideo(accessToken, shortVideo)
        }
    }

    fun getShortVideoData(
        accessToken: String,
        pageNum: Int = 1,
        pageSize: Int = 20,
        search: String = "",
        sort: String = "",
        isNext: Boolean = false
    ){
        viewModelScope.launch {
            repository.getShortVideoData(pageNum, pageSize, search, sort, accessToken, isNext)
        }
    }
    }