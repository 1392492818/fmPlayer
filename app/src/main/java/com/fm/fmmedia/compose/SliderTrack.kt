package com.fm.fmmedia.compose

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.Slider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp


@Composable
@ExperimentalMaterial3Api
fun Track(
    sliderPositions: SliderPositions,
    modifier: Modifier = Modifier,
    cacheProgress: Float = 0f,
    enabled: Boolean = true,
) {

    val cacheColor =  rememberUpdatedState(Color.LightGray)
    val inactiveTrackColor =  rememberUpdatedState( Color.Gray)
    val activeTrackColor = rememberUpdatedState(Color.White)
    val inactiveTickColor = rememberUpdatedState(Color.Black)
    val activeTickColor = rememberUpdatedState(Color.Yellow)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(5.dp)
    ) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val tickSize = 5.dp.toPx()
        val trackStrokeWidth = 5.dp.toPx()
        drawLine(
            inactiveTrackColor.value,
            sliderStart,
            sliderEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
        val cacheSliderValueEnd = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * cacheProgress,
            center.y
        )

        drawLine(
            cacheColor.value,
            sliderStart,
            cacheSliderValueEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
        val sliderValueEnd = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * sliderPositions.positionFraction,
            center.y
        )

        val sliderValueStart = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * 0f,
            center.y
        )

        drawLine(
            activeTrackColor.value,
            sliderValueStart,
            sliderValueEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
        sliderPositions.tickFractions.groupBy {
            it > sliderPositions.positionFraction ||
                    it < 0f
        }.forEach { (outsideFraction, list) ->
            drawPoints(
                list.map {
                    Offset(lerp(sliderStart, sliderEnd, it).x, center.y)
                },
                PointMode.Points,
                (if (outsideFraction) inactiveTickColor else activeTickColor).value,
                tickSize,
                StrokeCap.Round
            )
        }
    }
}