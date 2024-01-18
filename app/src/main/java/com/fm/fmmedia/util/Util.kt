package com.fm.fmmedia.util

fun generateListWithStep(step: Double, start:Double = 0.5, end: Double = 4.0): List<Double> {
    val result = mutableListOf<Double>()

    var current = start
    while (current <= end) {
        result.add(current)
        current += step
    }

    return result
}