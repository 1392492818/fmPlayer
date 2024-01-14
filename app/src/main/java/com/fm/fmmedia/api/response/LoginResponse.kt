package com.fm.fmmedia.api.response

import java.sql.Time
import java.util.Date

data class LoginResponse(
    val id: Int,
    val accessToken: String,
    val expiresTime: Int,
    val refreshToken: String,
    val userId:Int,
    val type: Int,
    val refreshExpiresTime: Int,
    val createTime: String,
    val status: Int
);