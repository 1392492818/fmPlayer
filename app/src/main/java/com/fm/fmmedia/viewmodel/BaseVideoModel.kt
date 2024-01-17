package com.fm.fmmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.repository.BaseRepository

open class BaseVideoModel(baseRepository: BaseRepository): ViewModel()  {
    val isRequestError: LiveData<Boolean> = baseRepository.isRequestError.asLiveData()

}