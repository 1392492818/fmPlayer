package com.fm.fmmedia.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.repository.LiveRepository
import com.fm.fmmedia.repository.LoginRepository
import kotlinx.coroutines.launch

class LiveViewModel(val liveRepository: LiveRepository) : BaseVideoModel(liveRepository) {
    val page = liveRepository.page.asLiveData()

    fun pageLive(
        pageNum: Int = 1,
        pageSize: Int = 20,
        search: String = "",
        sort: String = "",
        isNext: Boolean = false
    ) {
        viewModelScope.launch {
            liveRepository.getLiveData(
                pageNum = pageNum,
                pageSize = pageSize,
                search = search,
                sort = sort,
                isNext = isNext
            )
        }
    }

}