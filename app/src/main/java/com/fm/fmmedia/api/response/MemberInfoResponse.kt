package com.fm.fmmedia.api.response

data class MemberInfoResponse(
    val id: Int,
    val username: String,
    val phoneNumber: String,
    val email: String,
    val avatar: String,
    val lastLoginTime: String,
    val lastLoginIp: String,
    val registerTime: String,
    val registerIp: String,
    val sex: String
)