package com.fm.fmmedia.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.R
import com.fm.fmmedia.api.response.CategoryVideoResponse
import com.fm.fmmedia.compose.RequestError
import com.fm.fmmedia.compose.SearchBar
import com.fm.fmmedia.compose.SearchState
import com.fm.fmmedia.compose.SwipeRefresh
import com.fm.fmmedia.compose.videoItem
import com.fm.fmmedia.ui.Screen
import com.fm.fmmedia.repository.VideoCategoryRepository
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.viewmodel.VideoCategoryViewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class HomeSections(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
    HOME(R.string.home_home, Icons.Outlined.Home, Screen.Home.route),
    SHORT(R.string.home_short_video, Icons.Outlined.VideoLibrary, Screen.Short.route),
    LIVE(R.string.home_live, Icons.Default.LiveTv, Screen.Live.route),
    PROFILE(R.string.home_profile, Icons.Outlined.AccountCircle, Screen.Profile.route)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun homeScreen(
    navController: NavHostController,
    videoCategoryViewModel: VideoCategoryViewModel = VideoCategoryViewModel(
        VideoCategoryRepository()
    ),
    onVideoGroupClick: (name: Int) -> Unit
) {
    val systemUiController: SystemUiController = rememberSystemUiController()
//    systemUiController.setStatusBarColor(color= Color.Black)
    systemUiController.setStatusBarColor(color = Color.White)
    var presses by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val state: SearchState = remember {
        SearchState(
            query = TextFieldValue(""),
            focused = false,
            searching = false,
        )
    }


    val videoCategoryPage by videoCategoryViewModel.videoCategoryPage.observeAsState()
    val isRequestError by videoCategoryViewModel.isRequestError.observeAsState()
    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null) {
                FmBottomNavLayout(HomeSections.values(), currentRoute, navController)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(0.dp, 0.dp, 0.dp, 58.dp)
                .systemBarsPadding()
        ) {
            SearchBar(
                query = state.query,
                onQueryChange = { state.query = it },
                searchFocused = state.focused,
                onSearchFocusChange = { state.focused = it },
                onClearQuery = { state.query = TextFieldValue("") },
                searching = state.searching,
                onBack = { state.focused = false }
            )
            Divider()
            val scope = rememberCoroutineScope()
            var isRefreshing by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }
            var isFinishing by remember {
                mutableStateOf(false)
            }
//            val videoCategories by remember {
//                Log.e("数据", videoCategoryPage?.getData<List<CategoryVideoResponse>>().toString())
//                mutableStateOf(videoCategoryPage?.getData<List<CategoryVideoResponse>>())
//            }
            LaunchedEffect(videoCategoryPage) {
                videoCategoryPage?.let {
                    isFinishing = !it.hasNextPage
                }
            }
            if (isRequestError == false) {
                SwipeRefresh(
                    items = videoCategoryPage?.getData<List<CategoryVideoResponse>>(),
                    refreshing = isRefreshing,
                    loading = isLoading,
                    finishing = isFinishing,
//                    modifier = Modifier.fillMaxWidth(),
                    onRefresh = {
                        isRefreshing = true
                        isFinishing = false
                        scope.launch {
                            delay(1000)
                            videoCategoryViewModel.getVideoCategory()
                            isRefreshing = false
                        }
                    },
                    onLoad = {
                        scope.launch {
                            delay(1000)
                            if (videoCategoryPage?.hasNextPage == false) {
                                isFinishing = true
                            } else {
                                val pageNum = videoCategoryPage?.nextPage
                                val pageSize = videoCategoryPage?.pageSize
                                if (pageNum != null && pageSize != null) {
                                    videoCategoryViewModel.getVideoCategory(
                                        pageNum = pageNum,
                                        pageSize = pageSize,
                                        isNext = true
                                    )
                                }
                            }
                        }

//                    isLoading = false
                    }) { index, videoCategory ->

                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = videoCategory.name,
                                style = MaterialTheme.typography.h6,
                                color = FmMediaTheme.colors.brand,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.Start)
                            )
                        }
                        if (!videoCategory.videoGroup.isEmpty()) {

                            LazyRow(modifier = Modifier.height(180.dp)) {
                                items(videoCategory.videoGroup) { videoGroup ->
                                    videoItem(
                                        modifier = Modifier
                                            .height(180.dp)
                                            .width(screenWidth / 2)
                                            .padding(10.dp)
                                            .clickable {
                                                onVideoGroupClick(videoGroup.id)
                                            },
                                        name = videoGroup.name,
                                        imageUrl = BuildConfig.API_BASE_URL + "image/" + videoGroup.cover,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {
                            videoItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(10.dp),
                                name = "",
                                imageUrl = R.drawable.no_cover
                            )
                        }
                        Divider()
                    }

                }
            } else {

                RequestError(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable {
                        videoCategoryViewModel.getVideoCategory()
                    })
            }

        }
    }
}


@Composable
fun FmBottomNavLayout(tabs: Array<HomeSections>, route: String, navController: NavHostController) {

    val bottomAppBarHeight = 58.dp
    BottomAppBar(
        backgroundColor = FmMediaTheme.colors.iconPrimary,
        contentColor = FmMediaTheme.colors.iconPrimary,
        modifier = Modifier.systemBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomAppBarHeight)
        ) {
            tabs.forEach { it ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(bottomAppBarHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.clickable {
                            navController.navigate(it.route) {
                                launchSingleTop = true
                                popUpTo(Screen.Home.route) {
//                                inclusive = true
                                }
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            it.icon,
                            contentDescription = it.name,
                            tint = Color.White
                        )
                        if (route == it.route)
                            Text(text = stringResource(id = it.title), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

