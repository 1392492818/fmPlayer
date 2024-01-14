package com.fm.fmmedia.api.response

data class CategoryVideoResponse(
    val id: Int,
    val name: String,
    val createTime: String,
    val updateTime: String,
    val videoGroup: List<VideoGroupResponse>
)