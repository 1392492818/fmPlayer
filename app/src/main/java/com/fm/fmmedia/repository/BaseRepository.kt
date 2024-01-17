package com.fm.fmmedia.repository

import com.fm.fmmedia.api.response.VideoGroupResponse
import kotlinx.coroutines.flow.MutableStateFlow

open class BaseRepository {
    val isRequestError: MutableStateFlow<Boolean> =
        MutableStateFlow<Boolean>(false)
}