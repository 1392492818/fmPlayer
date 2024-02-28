package com.fm.fmmedia.util

import android.util.Log

fun generateListWithStep(step: Double, start:Double = 0.5, end: Double = 4.0): List<Double> {
    val result = mutableListOf<Double>()

    var current = start
    while (current <= end) {
        result.add(current)
        current += step
    }

    return result
}


fun isValidEmail(email: String): Boolean {

    val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

    Log.e("测试","${email}${email.matches(emailRegex)}")
    return email.matches(emailRegex)
}