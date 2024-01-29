package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.CategoryVideoResponse
import com.fm.fmmedia.api.response.MemberInfoResponse
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import kotlinx.coroutines.flow.MutableStateFlow


class MemberInfoRepository :BaseRepository() {
    val memberInfo: MutableStateFlow<MemberInfoResponse?> =
        MutableStateFlow<MemberInfoResponse?>(null)

    val TAG:String = VideoGroupRepository::class.simpleName.toString();

    suspend fun memberInfo(
        accessToken: String
    ) {
        try {
            isRequestError.value = false
            val result: Result = ApiRequest.create().memberInfo(accessToken);
            Log.e(TAG, result.toString())
            if(result.code == 0) {
                memberInfo.value  = result.parseData<MemberInfoResponse>()
            }

        } catch (e: Exception) {
            isRequestError.value = true
            Log.e(TAG, "请求异常"+ e.message)
        }
    }


}