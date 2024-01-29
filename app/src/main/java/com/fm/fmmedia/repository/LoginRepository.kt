package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.response.LoginResponse
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.data.AccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LoginRepository :BaseRepository() {
    val loginResponse: MutableStateFlow<LoginResponse?> =  MutableStateFlow<LoginResponse?>(null)

    suspend fun login(account:String, password:String) {
        try {
            reset()
            val result: Result = ApiRequest.create().login(Login(account, password));
            if(result.code != 0) {
                errorCode.value = result.code
                errorMsg.value = result.msg
                Log.e("测试", errorMsg.value)
            } else {
                loginResponse.value = result.parseData<LoginResponse>()
            }
        } catch (e:Exception) {
            isRequestError.value = true
        }


    }

    suspend fun clear(){
        reset()
        loginResponse.value = null
    }

}
