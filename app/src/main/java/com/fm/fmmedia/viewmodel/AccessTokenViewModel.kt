package com.fm.fmmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.repository.AccessTokenRepository
import com.fm.fmmedia.repository.VideoCategoryRepository
import kotlinx.coroutines.launch

class AccessTokenViewModel(private val accessTokenRepository: AccessTokenRepository) :BaseVideoModel(accessTokenRepository) {
    val accessTokenList: LiveData<List<AccessToken>> = accessTokenRepository.accessTokenList.asLiveData()

    suspend fun insert(accessToken: AccessToken){
        accessTokenRepository.insert(accessToken)
    }

     fun delete(id:Int) {
         viewModelScope.launch {
             accessTokenRepository.delete(id)
         }
    }
}

class AccessTokenModelFactory(private val accessTokenRepository: AccessTokenRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccessTokenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccessTokenViewModel(accessTokenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}