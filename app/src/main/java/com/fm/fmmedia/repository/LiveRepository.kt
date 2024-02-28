package com.fm.fmmedia.repository

import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import kotlinx.coroutines.flow.MutableStateFlow

class LiveRepository : BaseRepository() {
    val page: MutableStateFlow<Page?> =
        MutableStateFlow<Page?>(null)
    suspend fun getLiveData(
        pageNum: Int,
        pageSize: Int,
        search: String,
        sort: String,
        isNext: Boolean = false
    ) {
        try {
            isRequestError.value = false
            val result: Result = ApiRequest.create().live(
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
}