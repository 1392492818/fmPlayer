package com.fm.fmmedia.compose

import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.fm.fmmedia.R

@Composable
fun loading(modifier: Modifier = Modifier.fillMaxSize(), @ColorRes color: Int = R.color.black){
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = colorResource(id = color)
        )
    }
}