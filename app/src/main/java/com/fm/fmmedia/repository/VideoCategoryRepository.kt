package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.CategoryVideoResponse
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import kotlinx.coroutines.flow.MutableStateFlow




class VideoCategoryRepository :BaseRepository() {
    val page: MutableStateFlow<Page?> =
        MutableStateFlow<Page?>(null)

    val TAG:String = VideoGroupRepository::class.simpleName.toString();

    suspend fun getData(
        pageNum: Int,
        pageSize: Int,
        search: String,
        sort: String,
        isNext: Boolean = false
    ) {
        try {
            isRequestError.value = false
            val result: Result = ApiRequest.create().categoryVideo(pageNum, pageSize, search, sort);
            val videoCategoryPage = result.parseData<Page>()
            if (isNext) {
                val oldList: List<Any>? = page.value?.list
                val nextList: List<Any>? = videoCategoryPage?.list
                if (oldList != null && nextList != null) {
                    videoCategoryPage?.list = oldList.plus(nextList)
                    page.value = videoCategoryPage
                }
            } else {
                page.value = videoCategoryPage
            }
        } catch (e: Exception) {
            page.value = null
            isRequestError.value = true
            Log.e(TAG, "请求异常"+ e.message)
        }
    }


}