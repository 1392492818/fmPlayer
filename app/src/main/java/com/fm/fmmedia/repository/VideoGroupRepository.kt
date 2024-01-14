package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.response.VideoGroupResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.*

class VideoGroupRepository {
    val videoGroupList: MutableStateFlow<List<VideoGroupResponse>> =
        MutableStateFlow<List<VideoGroupResponse>>(emptyList())

    val videoGroup: MutableStateFlow<VideoGroupResponse?> = MutableStateFlow<VideoGroupResponse?>(null)
    val TAG = VideoGroupRepository::class.simpleName
    suspend fun getData(
        pageNum: Int,
        pageSize: Int,
        search: String,
        sort: String
    ) {
        val result: Result = ApiRequest.create().videoGroup(pageNum, pageSize, search, sort);
        val videoGroupPage = result.parseData<Page>()
        if (videoGroupPage != null) {
            val data: List<VideoGroupResponse>? = videoGroupPage.getData<List<VideoGroupResponse>>()
            if(data!= null)
                videoGroupList.value = data
        }
    }




    suspend fun findIdData(id: Int){
        try {
            val result: Result = ApiRequest.create().findIdVideoGroup(id);
            val videoGroupData = result.parseData<VideoGroupResponse>()
            videoGroup.value = videoGroupData
        }catch (e:Exception){
            e.message?.let { Log.e(TAG, it) }
        }

    }
}