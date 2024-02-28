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
    val isDeleteSuccess = repository.isDeleteSuccess.asLiveData()
    val page: LiveData<Page?> = repository.page.asLiveData()
    val pageAll: LiveData<Page?> = repository.pageAll.asLiveData()

    fun reset() {
        repository.resetAddParams()
    }

    fun addShortVideo(accessToken: String, shortVideo: ShortVideo) {
        viewModelScope.launch {
            repository.addShortVideo(accessToken, shortVideo)
        }
    }

    fun deleteShortVideo(accessToken: String, id: Int) {
        viewModelScope.launch {
            repository.deleteShortVideo(accessToken, id)
        }
    }

    fun getShortVideoData(
        accessToken: String,
        pageNum: Int = 1,
        pageSize: Int = 20,
        search: String = "",
        sort: String = "",
        isNext: Boolean = false
    ) {
        viewModelScope.launch {
            repository.getShortVideoData(pageNum, pageSize, search, sort, accessToken, isNext)
        }
    }

    fun getShortVideoAllData(
        pageNum: Int = 1,
        pageSize: Int = 20,
        search: String = "",
        sort: String = "",
        isNext: Boolean = false
    ) {
        viewModelScope.launch {
            repository.getShortVideoAllData(pageNum, pageSize, search, sort, isNext)
        }
    }
}