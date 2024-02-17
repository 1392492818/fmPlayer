package com.fm.fmmedia.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fm.fmmedia.repository.LoginRepository
import com.fm.fmmedia.repository.MemberInfoRepository
import com.fm.fmmedia.repository.VideoGroupRepository
import com.fm.fmmedia.ui.login.forgetPassword
import com.fm.fmmedia.ui.login.loginScreen
import com.fm.fmmedia.ui.login.registerScreen
import com.fm.fmmedia.ui.home.homeScreen
import com.fm.fmmedia.ui.live.liveScreen
import com.fm.fmmedia.ui.profile.profileScreen
import com.fm.fmmedia.ui.record.RecordScreen
import com.fm.fmmedia.ui.record.VideoEditScreen
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.ui.video.videoScreen
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.LoginViewModel
import com.fm.fmmedia.viewmodel.MemberInfoViewModel
import com.fm.fmmedia.viewmodel.VideoCategoryViewModel
import com.fm.fmmedia.viewmodel.VideoGroupViewModel
import java.net.URLEncoder


@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "profile"
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable("profile") {
            ProfileScreen(
                onNavigateToFriends = { navController.navigate("friendsList") },
                /*...*/
            )
        }
        composable("friendslist") {
            Log.e("测试", "friendslist")
            FriendsListScreen()
        }
    }
}

@Composable
fun FriendsListScreen(

    /*...*/
) {
    /*...*/
    Log.e("测试", "FriendsListScreen")
    Text(text = "FriendsListScreen")

}

@Composable
fun ProfileScreen(
    onNavigateToFriends: () -> Unit,
    /*...*/
) {
    /*...*/
    Button(onClick = onNavigateToFriends) {
        Text(text = "See friends list")
    }
}


@Composable
fun fmAndroidApp(
    activity: Activity,
    videoCategoryViewModel: VideoCategoryViewModel,
    accessTokenViewModel: AccessTokenViewModel
) {
    val navController = rememberNavController()


    FmMediaTheme {
        Surface {
//            forgetPassword()
            fmNavHost(
                activity = activity,
                navController = navController,
                videoCategoryViewModel = videoCategoryViewModel,
                accessTokenViewModel = accessTokenViewModel
            )
        }
    }
}


@Composable
fun fmOnNewIntent(intent: Intent) {
    val navController = rememberNavController()
    navController.handleDeepLink(intent)
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun fmNavHost(
    activity: Activity,
    navController: NavHostController,
    videoCategoryViewModel: VideoCategoryViewModel,
    accessTokenViewModel: AccessTokenViewModel
) {
    videoCategoryViewModel.getVideoCategory() //初始化数据
    val videoGroupViewModel = VideoGroupViewModel(
        VideoGroupRepository()
    )
    val loginViewModel: LoginViewModel = LoginViewModel(LoginRepository())
    val memberInfoViewModel = MemberInfoViewModel(MemberInfoRepository())

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Login.route) {
            loginScreen(
                onForgetPassword = {
                    navController.navigate(Screen.ForgetPassword.createRoute("测试")) {
                        launchSingleTop = true
                    }
                },
                onRegister = {
                    navController.navigate(Screen.Register.route) { launchSingleTop = true }
                },
                onHome = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onBack = {},
                loginViewModel = loginViewModel,
                accessTokenViewModel = accessTokenViewModel
            )
        }
        composable(
            route = Screen.Register.route,
            deepLinks = Screen.Register.deepLinks
        ) {
            registerScreen()
        }
        composable(route = Screen.ReCord.route){
            RecordScreen(onVideoUpload = {path->
                navController.navigate(Screen.VideoEdit.createRoute(URLEncoder.encode(path, "utf-8")))
            })
        }
        composable(route = Screen.VideoEdit.route,  arguments = Screen.VideoEdit.navArguments) {
            val path: String? = it.arguments?.getString("path")
            path?.let { it1 -> VideoEditScreen(it1) }
        }
        composable(
            route = Screen.ForgetPassword.route,
            arguments = Screen.ForgetPassword.navArguments,
            deepLinks = Screen.ForgetPassword.deepLinks
        ) {
            val id: String = it.arguments?.getString("id").toString()
            Log.e("fuweicong", id);
            forgetPassword()
        }
        composable(route = Screen.Home.route) {
            homeScreen(
                navController,
                videoCategoryViewModel = videoCategoryViewModel,
                onVideoGroupClick = { id ->
                    Log.e("home 跳转", id.toString())
                    navController.navigate(Screen.Video.createRoute(id))
                }
            )

//            RecordScreen(onVideoUpload = {path->
//                navController.navigate(Screen.VideoEdit.createRoute(URLEncoder.encode(path, "utf-8")))
//            })
        }
        composable(route = Screen.Live.route) {
            liveScreen(navController = navController)
        }
        composable(route = Screen.Profile.route) {
            val accessTokenList by accessTokenViewModel.accessTokenList.observeAsState()
            if (accessTokenList?.isEmpty() == true) {
                loginScreen(onForgetPassword = {
                    navController.navigate(Screen.ForgetPassword.createRoute("测试")) {
                        launchSingleTop = true
                    }
                }, onRegister = {
                    navController.navigate(Screen.Register.route) { launchSingleTop = true }
                }, onHome = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }, onBack = {
                    navController.popBackStack()
                }, loginViewModel = loginViewModel, accessTokenViewModel = accessTokenViewModel)
            } else {
                profileScreen(
                    navController = navController,
                    accessTokenViewModel = accessTokenViewModel,
                    memberInfoViewModel = memberInfoViewModel,
                    onRecord = {
                        navController.navigate(Screen.ReCord.route)
                    }
                )
            }
        }
        composable(route = Screen.Video.route, arguments = Screen.Video.navArguments) {
            val id: Int? = it.arguments?.getInt("id")
            Log.e("id", id.toString());
            if (id != null) {
                videoScreen(
                    id = id,
                    navController = navController,
                    videoGroupViewModel = videoGroupViewModel
                )
//                videoScreen(id = id)
            }
        }
    }
}