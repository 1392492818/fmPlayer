package com.fm.fmmedia.api.request

data class Register(
    val email: String,
    val password: String,
    val verificationCode: String

)