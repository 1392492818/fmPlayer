package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.request.ShortVideo
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.response.ShortVideoResponse
import kotlinx.coroutines.flow.MutableStateFlow

class ShortVideoRepository : BaseRepository() {
    val isAddSuccess: MutableStateFlow<Boolean> =
        MutableStateFlow<Boolean>(false)

    val page: MutableStateFlow<Page?> =
        MutableStateFlow<Page?>(null)

    val pageAll: MutableStateFlow<Page?> =
        MutableStateFlow<Page?>(null)

    val isDeleteSuccess: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)

    fun resetAddParams(){
        isAddSuccess.value = false
    }

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

    fun deleteId(id: Int){
        var shortVideoPage = page.value
        val list = shortVideoPage?.getData<List<ShortVideoResponse>>()
        val shortVideoResponse: ShortVideoResponse? = list?.find { shortVideoResponse ->
             shortVideoResponse.id == id
        }
        shortVideoResponse?.let {shortVideoResponse
            val index = list?.indexOf(shortVideoResponse)
            index?.let {
                Log.e("delete删除成功", "删除成功index:${index}, ${page.value?.list?.size}")
                shortVideoPage?.list?.removeAt(index)
                if (shortVideoPage != null) {
                    shortVideoPage.size = shortVideoPage?.list?.size!!
                }
                page.value = shortVideoPage
                Log.e("delete删除成功", "删除成功index:${index}, ${page.value?.size}")
            }
        }

        return

    }
    suspend fun deleteShortVideo(accessToken: String, id:Int){
        try {
            reset()
            val result = ApiRequest.create().deleteShortVideo(accessToken, id);
            if (result.code == 0) {
                deleteId(id)
                isDeleteSuccess.value = true
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
                    shortVideoPage?.list = oldList.plus(nextList).toMutableList()
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


    suspend fun getShortVideoAllData(
        pageNum: Int,
        pageSize: Int,
        search: String,
        sort: String,
        isNext: Boolean = false
    ) {
        try {
            isRequestError.value = false
            val result: Result = ApiRequest.create().getAllShortVideo(
                pageNum = pageNum,
                pageSize = pageSize,
                search = search,
                sort = sort
            );
            val shortVideoPage = result.parseData<Page>()
            if (isNext) {
                val oldList: List<Any>? = pageAll.value?.list
                val nextList: List<Any>? = shortVideoPage?.list
                if (oldList != null && nextList != null) {
                    shortVideoPage?.list = oldList.plus(nextList).toMutableList()
                    pageAll.value = shortVideoPage
                }
            } else {
                pageAll.value = shortVideoPage
            }
        } catch (e: Exception) {
            pageAll.value = null
            isRequestError.value = true
        }
    }

}