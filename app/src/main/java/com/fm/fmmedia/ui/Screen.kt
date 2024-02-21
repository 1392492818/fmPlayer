/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fm.fmmedia.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    object Login : Screen("Login")

    object Home : Screen("Home")

    object ReCord: Screen("Record")

    object VideoEdit: Screen(route = "VideoEdit/{path}",   navArguments = listOf(navArgument("path") {
        type = NavType.StringType
    })
    ) {
        fun createRoute(path: String) = "VideoEdit/${path}"
    }

    object VideoUpload: Screen(route = "VideoUpload/{path}",   navArguments = listOf(navArgument("path") {
        type = NavType.StringType
    })
    ) {
        fun createRoute(path: String) = "VideoUpload/${path}"
    }

    object Live : Screen("Live")

    object Profile : Screen("Profile")

    object Video : Screen(
        route = "Video/{id}",
        navArguments = listOf(navArgument("id") {
            type = NavType.IntType
        })
    ) {
        fun createRoute(name: Int) = "Video/${name}"
    }

    object Register : Screen("register") {
        val deepLinks = listOf(
            navDeepLink { uriPattern = "fm://$route" }
        )
    }

    object ForgetPassword : Screen(
        route = "forget_password/{id}",
        navArguments = listOf(navArgument("id") {
            type = NavType.StringType
        })
    ) {
        fun createRoute(id: String) = "forget_password/${id}"
        val deepLinks = listOf(
            navDeepLink { uriPattern = "fm://$route" }
        )
    }


}