package com.fm.fmmedia.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.R
import com.fm.fmmedia.api.response.Page
import com.fm.fmmedia.api.response.ShortVideoResponse
import com.fm.fmmedia.compose.videoItem
import com.fm.fmmedia.ui.Screen


@Composable
fun emptyData(modifier: Modifier = Modifier.fillMaxWidth()
    .padding(10.dp)){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.empty_data), modifier = Modifier.padding(10.dp))
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.empty_data),
            tint = Color.Black,
            contentDescription = "",
        )
    }
}

@Composable
fun myVideo(
    page: Page? = null,
    columnNum: Int = 3,
    columnItemHeight: Dp = 180.dp,
    onClick: (index:Int)->Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val shortVideoList = page?.getData<List<ShortVideoResponse>>()
    shortVideoList?.let { shortVideoList ->
        shortVideoList.chunked(columnNum)
            .forEachIndexed { chunkIndex, chunk ->
                Row(modifier = Modifier.height(columnItemHeight)) {
                    chunk.forEachIndexed { index, shortVideo ->
                        val globalIndex = chunkIndex * 3 + index
                        videoItem(
                            modifier = Modifier
                                .height(columnItemHeight)
                                .width(screenWidth / columnNum)
                                .padding(2.dp)
                                .clickable {
                                    onClick(globalIndex)
                                },
                            name = shortVideo.title,
                            imageUrl = BuildConfig.API_BASE_URL + "image/" + shortVideo.cover,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
    }
    if(shortVideoList == null || shortVideoList.isEmpty()) {
        emptyData()
    }
}


@Composable
fun likeVideoList(){
    emptyData()
}

@Composable
fun collectVideoList() {
    emptyData()
}