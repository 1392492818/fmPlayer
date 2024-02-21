package com.fm.fmmedia.ui.record

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fm.fmmedia.R
import com.fm.fmmedia.api.request.ShortVideo
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.repository.FileUploadRepository
import com.fm.fmmedia.repository.ShortVideoRepository
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.FileUploadViewModel
import com.fm.fmmedia.viewmodel.ShortVideoViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun videoUploadScreen(
    navController: NavController,
    path: String,
    accessTokenViewModel: AccessTokenViewModel,
    redirect: () -> Unit
) {
    var desc by remember {
        mutableStateOf("")
    }
    val softwareKeyboardController = LocalSoftwareKeyboardController.current;
    val fileUploadViewModel = FileUploadViewModel(FileUploadRepository())
    val videoFileUploadStatus by fileUploadViewModel.videoUploadStatus.observeAsState()
    val videoFileUploadPath = fileUploadViewModel.videoUploadPath.observeAsState()
    val imageFileUploadStatus by fileUploadViewModel.imageUploadStatus.observeAsState()
    val imageFileUploadPath = fileUploadViewModel.imageUploadPath.observeAsState()
    val isFileUploadError by fileUploadViewModel.isRequestError.observeAsState()
    var isUploadLoading by remember {
        mutableStateOf(false)
    }
    val accessTokenList by accessTokenViewModel.accessTokenList.observeAsState()
    val shortVideoViewModel = ShortVideoViewModel(ShortVideoRepository())
    val isAddShortVideo by shortVideoViewModel.isAddSuccess.observeAsState()
    val isAddShortVideoError by shortVideoViewModel.isRequestError.observeAsState()
    val accessToken = accessTokenList?.first()
    val context = LocalContext.current
    val retriever = MediaMetadataRetriever()
    val filesDir = context.filesDir.absolutePath
    var progress by remember {
        mutableStateOf(0)
    }

    try {
        retriever.setDataSource(path)
    } catch (e: Exception) {

    }

    // 提取缩略图

    // 提取缩略图
    val thumbnail = retriever.frameAtTime
    var focusRequester = remember {
        FocusRequester()
    }
    var stringInfo by remember {
        mutableStateOf(R.string.video_screen_edit_cancel)
    }
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            DisposableEffect(key1 = isFileUploadError, key2 = isAddShortVideoError) {
                if (isFileUploadError == true || isAddShortVideoError == true) {
                    isUploadLoading = false
                }
                onDispose { }
            }

            DisposableEffect(key1 = videoFileUploadStatus, key2 = imageFileUploadStatus) {
                if (videoFileUploadStatus == true && imageFileUploadStatus == true) {
                    val cover: String = imageFileUploadPath.value.toString()
                    val source: String = videoFileUploadPath.value.toString()
                    if (accessToken != null) {
                        shortVideoViewModel.addShortVideo(
                            accessToken = accessToken.accessToken, ShortVideo(
                                title = "",
                                desc = desc,
                                like = 0,
                                collect = 0,
                                cover = cover,
                                source = source
                            )
                        )
                    }

                }
                onDispose { }
            }

            DisposableEffect(isAddShortVideo) {
                isUploadLoading = false
                if (isAddShortVideo == true) {
                    redirect()
                }
                onDispose { }
            }

            Column(modifier = Modifier.padding(paddingValues)) {
                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = stringResource(id = stringInfo),
                        modifier = Modifier
                            .clickable {
                                if (stringInfo == R.string.video_screen_edit_cancel) {
                                    navController.popBackStack()
                                }
                                softwareKeyboardController?.hide()
                                focusRequester.freeFocus()
                            })

                    Button(
                        onClick = {
                            accessToken?.accessToken?.let { accessToken ->
                                isUploadLoading = true
                                GlobalScope.launch {
                                    thumbnail?.let {
                                        val file = fileUploadViewModel.saveBitmapAsFile(
                                            context = context,
                                            bitmap = thumbnail,
                                            fileName = "thumb.png"
                                        )
                                        file?.let {
                                            if (imageFileUploadStatus == false)
                                                fileUploadViewModel.imageUpload(
                                                    file.absolutePath,
                                                    accessToken
                                                )
                                        }
                                    }

                                    if (videoFileUploadStatus == false)
                                        fileUploadViewModel.videoUpload(
                                            path = path,
                                            accessToken = accessToken,
                                            progressCallback = { it ->
                                                progress = it
                                            }
                                        )
                                }

                            }

                        }, modifier = Modifier
                            .width(60.dp)
                            .height(40.dp)
                            .padding(5.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(5.dp), // 圆角形状，8dp的圆角
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FmMediaTheme.colors.iconPrimary, // 按钮的背景颜色
                            contentColor = Color.White // 按钮的内容颜色（文本和图标）
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.video_screen_edit_submit),
                            fontSize = 10.sp
                        )
                    }

                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(10.dp)
                ) {

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(Color.White)
                            .focusRequester(focusRequester),

                        placeholder = {
                            Text(
                                stringResource(id = R.string.video_screen_edit_placeholder),
                                fontSize = 12.sp
                            )
                        },
                        value = desc,
                        onValueChange = { value ->
                            desc = value
                            if (desc.isNotEmpty()) {
                                stringInfo = R.string.video_screen_edit_confirm
                            } else {
                                stringInfo = R.string.video_screen_edit_cancel
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = FmMediaTheme.colors.iconPrimary, // 设置焦点时的边框颜色
                            unfocusedBorderColor = FmMediaTheme.colors.iconPrimary,
                            cursorColor = FmMediaTheme.colors.iconPrimary,
                            selectionColors = TextSelectionColors(
                                handleColor = Color.Black.copy(alpha = 0.6f),
                                backgroundColor = Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )

                    thumbnail?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "缩略图",
                            modifier = Modifier
                                .width(90.dp)
                                .height(120.dp)
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        )
                    }

                }

            }

            if (isUploadLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .clickable { }
                        .background(Color.Black.copy(0.2f)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    loading(modifier = Modifier
                        .size(50.dp)
                        )
                    Text(text = "$progress")
                }
            }

        }
    }
}


