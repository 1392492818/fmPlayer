package com.fm.fmmedia.api.response

data class LiveResponse(
    val id: Int,
    val app: String,
    val stream: String,
    val online: Int,
    val createTime: String,
    val updateTime: String,
    val readerCount: Int,
    val memberId: Int,
    val member: MemberInfoResponse,
    val streamSchema: String
)