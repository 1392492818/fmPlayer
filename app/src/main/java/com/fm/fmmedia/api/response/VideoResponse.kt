package com.fm.fmmedia.api.response

data class VideoResponse(
    val id: Int,
    val name: String,
    val source: String,
    val cost: Double,
    val createTime: String,
    val updateTime: String,
    val order: Int,
)