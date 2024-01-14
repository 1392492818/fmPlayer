package com.fm.fmmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel(){

     val loginLiveData = MutableLiveData<LoginResponse?>()

//    var loginLiveData:LiveData<LoginResponse?> = _loginLiveData;

    fun login(account:String, password:String) {
        viewModelScope.launch{
            val result: Result = ApiRequest.create().login(Login(account, password));
            val loginResponse = result.parseData<LoginResponse>()
            if (loginResponse != null) {
                loginLiveData.postValue(loginResponse)
            }
        }
    }
}