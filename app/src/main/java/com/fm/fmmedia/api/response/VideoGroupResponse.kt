package com.fm.fmmedia.api.response

data class VideoGroupResponse(
    val id: Int,
    val name: String,
    val createTime: String,
    val updateTime: String,
    val video: List<VideoResponse>
)