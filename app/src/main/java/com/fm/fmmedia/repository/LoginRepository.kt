package com.fm.fmmedia.repository

import android.util.Log
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.request.Register
import com.fm.fmmedia.api.request.RegisterEmail
import com.fm.fmmedia.api.response.LoginResponse
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.data.AccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LoginRepository :BaseRepository() {
    val loginResponse: MutableStateFlow<LoginResponse?> =  MutableStateFlow<LoginResponse?>(null)
    val registerResponse: MutableStateFlow<LoginResponse?> = MutableStateFlow<LoginResponse?>(null)
    val registerEmailResponse: MutableStateFlow<Result?> = MutableStateFlow<Result?>(null)
    fun setErrorCode(result: Result):Boolean {
        if(result.code != 0) {
            errorCode.value = result.code
            errorMsg.value = result.msg
            Log.e("测试", errorMsg.value)
            return false
        }
        return true;
    }

    suspend fun login(account:String, password:String) {
        try {
            reset()
            val result: Result = ApiRequest.create().login(Login(account, password));
            if (setErrorCode(result)){
                loginResponse.value = result.parseData<LoginResponse>()
            }
        } catch (e:Exception) {
            isRequestError.value = true
        }
    }

    suspend fun register(register: Register){
        try {
            val result: Result = ApiRequest.create().register(register)
            if(setErrorCode(result)) {
                registerResponse.value = result.parseData<LoginResponse?>()
            }
        }catch (e:Exception){
            isRequestError.value = true
        }
    }

    suspend fun registerEmail(email:String){
        try {
            Log.e("测试","账号验证")
            val result: Result = ApiRequest.create().registerEmail(RegisterEmail( email))
            if(setErrorCode(result)) {
                registerEmailResponse.value = result
            }
        }catch (e:Exception) {
            isRequestError.value = true
        }
    }

    suspend fun clear(){
        reset()
        errorCode.value = 0
        registerResponse.value = null
        loginResponse.value = null
    }

}
