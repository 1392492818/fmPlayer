package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.response.VideoGroupResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.*

class VideoGroupRepository: BaseRepository() {
    val page: MutableStateFlow<Page?> =
        MutableStateFlow<Page?>(null)

    val videoGroup: MutableStateFlow<VideoGroupResponse?> = MutableStateFlow<VideoGroupResponse?>(null)
    val TAG = VideoGroupRepository::class.simpleName
    suspend fun getData(
        pageNum: Int,
        pageSize: Int,
        search: String,
        sort: String,
        isNext: Boolean = false
    ) {
        try { // 这个类型差不多，后面封装成一起
            val result: Result = ApiRequest.create().videoGroup(pageNum, pageSize, search, sort);
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
        }catch (e:Exception) {
            isRequestError.value = true
        }
    }



    suspend fun findIdData(id: Int){
        try {
            val result: Result = ApiRequest.create().findIdVideoGroup(id);
            val videoGroupData = result.parseData<VideoGroupResponse>()
            videoGroup.value = videoGroupData
        }catch (e:Exception){
            isRequestError.value = true
            e.message?.let { Log.e(TAG, it) }
        }
    }
}