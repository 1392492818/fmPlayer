package com.fm.fmmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.api.response.CategoryVideoResponse
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.VideoGroupResponse
import com.fm.fmmedia.repository.VideoCategoryRepository
import com.fm.fmmedia.repository.VideoGroupRepository
import com.fm.fmmedia.repository.WordRepository
import kotlinx.coroutines.launch

class VideoCategoryViewModel(val repository: VideoCategoryRepository) : ViewModel() {
    val videoCategoryPage: LiveData<Page?> = repository.page.asLiveData()

    fun getVideoCategory(
        pageNum: Int = 1,
        pageSize: Int = 5,
        search: String = "",
        sort: String = "",
        isNext: Boolean = false
    ) {
        viewModelScope.launch {
            repository.getData(pageNum, pageSize, search, sort, isNext)
        }
    }
}

class VideoCategoryModelFactory(private val repository: VideoCategoryRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoCategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoCategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}