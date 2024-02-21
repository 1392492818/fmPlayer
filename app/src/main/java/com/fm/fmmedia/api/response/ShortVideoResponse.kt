package com.fm.fmmedia.api.response

data class ShortVideoResponse(
    val id: Int,
    val title: String,
    val desc: String,
    val like: Int,
    val collect: Int,
    val cover: String,
    val source: String,
    val member: MemberInfoResponse
)