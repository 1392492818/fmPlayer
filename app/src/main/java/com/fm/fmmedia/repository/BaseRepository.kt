package com.fm.fmmedia.repository

import com.fm.fmmedia.api.response.VideoGroupResponse
import kotlinx.coroutines.flow.MutableStateFlow

open class BaseRepository {
    val isRequestError: MutableStateFlow<Boolean> =
        MutableStateFlow<Boolean>(false)

    val errorCode: MutableStateFlow<Int> = MutableStateFlow<Int>(0)

    val errorMsg: MutableStateFlow<String> = MutableStateFlow<String>("")

    fun reset(){
        isRequestError.value = false;
        errorCode.value = 0
    }
}