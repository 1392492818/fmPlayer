package com.fm.fmmedia.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fm.fmmedia.model.Member
import com.fm.fmmedia.model.VideoCollection
import com.fm.fmmedia.ui.theme.FmMediaTheme

private val HighlightCardWidth = 170.dp
private val HighlightCardPadding = 16.dp
private val Density.cardWidthWithPaddingPx
    get() = (HighlightCardWidth + HighlightCardPadding).toPx()


@Composable
fun videoItem(modifier: Modifier = Modifier.height(100.dp), name:String = "", imageUrl: Any = ""){
    Column(modifier = modifier) {
        Box(modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .weight(1f)
        ){
            Column {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "",
//                placeholder = painterResource(R.drawable.baseline_cloud_download_24),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.None,
                )
            }
        }
        Text(text = name, modifier = Modifier.padding(5.dp))
    }
}


@Preview
@Composable
fun test(){
    val videoCollection = VideoCollection(
         id = 1,
     stream = "",
     online = 1,
     createTime = "",
     updateTime = "",
     readerCount = 1,
     member = Member(
         id = 1,
         username = "",
         email = "",
         avatar = "",
         lastLoginTime = "",
         registerTime = ""
     ),
     streamSchema = ""

    )
    videoItem(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .height(100.dp)
            .width(150.dp)
            .background(FmMediaTheme.colors.brand)
    )
}