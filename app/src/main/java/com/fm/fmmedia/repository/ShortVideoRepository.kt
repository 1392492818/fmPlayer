package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.request.ShortVideo
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import kotlinx.coroutines.flow.MutableStateFlow

class ShortVideoRepository : BaseRepository() {
    val isAddSuccess: MutableStateFlow<Boolean> =
        MutableStateFlow<Boolean>(false)

    val page: MutableStateFlow<Page?> =
        MutableStateFlow<Page?>(null)

    suspend fun addShortVideo(accessToken: String, shortVideo: ShortVideo) {
        try {
            reset()
            val result = ApiRequest.create().addShortVideo(accessToken, shortVideo);
            if (result.code == 0) {
                isAddSuccess.value = true
            } else {
                errorCode.value = result.code
            }
        } catch (e: Exception) {
            isRequestError.value = true
        }
    }

    suspend fun getShortVideoData(
        pageNum: Int,
        pageSize: Int,
        search: String,
        sort: String,
        accessToken: String,
        isNext: Boolean = false
    ) {
        try {
            isRequestError.value = false
            val result: Result = ApiRequest.create().getShortVideo(
                accessToken = accessToken,
                pageNum = pageNum,
                pageSize = pageSize,
                search = search,
                sort = sort
            );
            val shortVideoPage = result.parseData<Page>()
            if (isNext) {
                val oldList: List<Any>? = page.value?.list
                val nextList: List<Any>? = shortVideoPage?.list
                if (oldList != null && nextList != null) {
                    shortVideoPage?.list = oldList.plus(nextList)
                    page.value = shortVideoPage
                }
            } else {
                page.value = shortVideoPage
            }
        } catch (e: Exception) {
            page.value = null
            isRequestError.value = true
        }
    }

}