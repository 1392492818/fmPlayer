package com.fm.fmmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.fm.fmmedia.api.ApiRequest
import com.fm.fmmedia.api.response.Result
import com.fm.fmmedia.api.request.Login
import com.fm.fmmedia.api.request.Register
import com.fm.fmmedia.api.response.LoginResponse
import com.fm.fmmedia.repository.LoginRepository
import com.fm.fmmedia.repository.VideoCategoryRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : BaseVideoModel(loginRepository){

     val loginLiveData = loginRepository.loginResponse.asLiveData()
     val registerEmail = loginRepository.registerEmailResponse.asLiveData()
     val registerResponse = loginRepository.registerResponse.asLiveData()
    fun login(account:String, password:String) {
        viewModelScope.launch{
            loginRepository.login(account, password)
        }
    }

    fun register(register: Register) {
        viewModelScope.launch {
            loginRepository.register(register)

        }
    }

    fun registerEmail(email:String){
        viewModelScope.launch {
            loginRepository.registerEmail(email)
        }
    }

    fun forgetPassword(){

    }

    fun clear(){
        viewModelScope.launch {
            loginRepository.clear()
        }
    }
}


