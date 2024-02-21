package com.fm.fmmedia.api.request

data class ShortVideo(
    val title: String,
    val desc: String,
    val like: Int,
    val collect: Int,
    val cover: String,
    val source: String
)