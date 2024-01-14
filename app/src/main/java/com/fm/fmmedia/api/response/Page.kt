package com.fm.fmmedia.api.response

import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Page(
    val total: Int,
    var list: List<Any>,
    val pageNum: Int,
    val pageSize: Int,
    val size: Int,
    val startRow: Int,
    val endRow: Int,
    val pages: Int,
    val prePage: Int,
    val nextPage: Int,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
    val hasPreviousPage: Boolean,
    val hasNextPage: Boolean,
    val navigatePages: Int,
    val navigatepageNums: Array<Int>,
    val navigateFirstPage: Int,
    val navigateLastPage: Int
) {
    inline fun <reified T> getData(): T? {
        try {
            val jsonString: String = Gson().toJson(list)
            Log.e("page测试", toString())
            val gson = Gson()
            val type = object : TypeToken<T>() {}.type
            return gson.fromJson(jsonString, type)
        } catch (e: ClassCastException) {
//                e.printStackTrace()
            return null
        }
    }
}