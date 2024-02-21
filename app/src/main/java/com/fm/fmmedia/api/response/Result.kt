package com.fm.fmmedia.api.response

import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonParser.parseString


data class Result(var code: Int, val msg:String, val data: Any){
    inline fun <reified T> parseData(): T? {
        if (code == 0) {
            try {
                val jsonString: String = Gson().toJson(data)
                Log.e("测试", toString())
                val gson = Gson()
                val type = object : TypeToken<T>() {}.type
                return gson.fromJson(jsonString, type)
            }catch (e: ClassCastException) {
//                e.printStackTrace()
                return null
            }
        } else {
            Log.e("测试", msg);
        }
        return null
    }
}