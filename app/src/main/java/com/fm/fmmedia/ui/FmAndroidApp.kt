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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fm.fmmedia.repository.VideoGroupRepository
import com.fm.fmmedia.ui.login.forgetPassword
import com.fm.fmmedia.ui.login.loginScreen
import com.fm.fmmedia.ui.login.registerScreen
import com.fm.fmmedia.ui.home.homeScreen
import com.fm.fmmedia.ui.live.liveScreen
import com.fm.fmmedia.ui.profile.profileScreen
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.ui.video.videoScreen
import com.fm.fmmedia.viewmodel.VideoCategoryViewModel
import com.fm.fmmedia.viewmodel.VideoGroupViewModel


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
fun fmAndroidApp(activity: Activity, videoCategoryViewModel: VideoCategoryViewModel) {
    val navController = rememberNavController()


    FmMediaTheme {
        Surface {
//            forgetPassword()
            fmNavHost(
                activity = activity,
                navController = navController,
                videoCategoryViewModel = videoCategoryViewModel
            )
        }
    }
}

@Composable
fun loading() {

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
    videoCategoryViewModel: VideoCategoryViewModel
) {
    videoCategoryViewModel.getVideoCategory() //初始化数据
    val videoGroupViewModel = VideoGroupViewModel(
        VideoGroupRepository()
    )
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Login.route) {
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
            })
        }
        composable(
            route = Screen.Register.route,
            deepLinks = Screen.Register.deepLinks
        ) {
            Log.e("dfas", "哈哈哈")
            registerScreen()
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
                })
        }
        composable(route = Screen.Live.route) {
            liveScreen(navController = navController)
        }
        composable(route = Screen.Profile.route) {
            profileScreen(navController = navController)
        }
        composable(route = Screen.Video.route, arguments = Screen.Video.navArguments) {
            val id: Int? = it.arguments?.getInt("id")
            Log.e("id", id.toString());
            if (id != null) {
                videoScreen(id = id, navController = navController, videoGroupViewModel = videoGroupViewModel)
//                videoScreen(id = id)
            }
        }
    }
}