package com.fm.fmmedia.model

data class VideoCollection(
    val id:Int,
    val stream:String,
    val online: Int,
    val createTime: String,
    val updateTime: String,
    val readerCount: Int,
    val member:Member,
    val streamSchema: String
)