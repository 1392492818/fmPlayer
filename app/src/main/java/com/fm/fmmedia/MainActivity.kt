package com.fm.fmmedia

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import com.fm.fmmedia.compose.FmGlView
import com.fm.fmmedia.ui.fmAndroidApp
import com.fm.fmmedia.viewmodel.AccessTokenModelFactory
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.VideoCategoryModelFactory
import com.fm.fmmedia.viewmodel.VideoCategoryViewModel
import com.fm.openglrender.OpenglRender

class MainActivity : ComponentActivity() {

    private val videoCategoryViewModel: VideoCategoryViewModel  by viewModels{
        VideoCategoryModelFactory((application as FmApplication).videoCategoryRepository)
    }

    private val accessTokenViewModel: AccessTokenViewModel by viewModels {
        AccessTokenModelFactory((application as FmApplication).accessTokenRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            enableEdgeToEdge()
            // 设置状态栏背景为指定颜色（这里使用红色作为示例）
//            DynamicColors.applyTo(context)
//            window?.statusBarColor = Color.White.toArgb()
            fmAndroidApp(this, videoCategoryViewModel, accessTokenViewModel)

//            FmMediaTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    myApp("Android")
//                }
//            }
//        }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
//        Log.e("测试", "onNewIntent");
//        {
//            fmOnNewIntent(intent)
//
//        }
//        navController.handleDeepLink(intent)
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        val expanded = remember { mutableStateOf(false) }
        Surface(color = MaterialTheme.colorScheme.primary) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = "Hello,")
                    Text(text = name)
                }
                ElevatedButton(
                    onClick = { expanded.value = !expanded.value },
                ) {
                    Text(if (expanded.value) "Show less" else "Show more")
                }
            }

        }
    }

    @Composable
    fun myApp(name: String) {
        var shouldShowOnboarding by remember { mutableStateOf(true) }
        Surface {
            if (shouldShowOnboarding) {
                OnboardingScreen(onContinueClicked = {
                    shouldShowOnboarding = false/*TODO*/
                }) //状态回调
            } else {
                OnListText(onContinueClicked = { shouldShowOnboarding = true/*TODO*/ })
            }

        }
    }

    @Composable
    fun OnListText(
        onContinueClicked: () -> Unit,
        modifier: Modifier = Modifier,
        names: List<String> = List(1000) { "$it" }
    ) {

        LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
            items(items = names) { name ->
                Column {
                    Greeting(name = name)
                    Greeting(name = "哈哈哈2")
                    Button(
                        modifier = Modifier.padding(vertical = 24.dp),
                        onClick = onContinueClicked
                    ) {
                        Text("Continue")
                    }
                }
            }
        }

    }

    @Composable
    fun OnboardingScreen(
        onContinueClicked: () -> Unit,
        modifier: Modifier = Modifier
    ) {

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Basics Codelab!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onContinueClicked
            ) {
                Text("Continue")
            }
        }
    }

//    @Preview(showBackground = true)
//    @Composable
//    fun GreetingPreview() {
//        FmMediaTheme {
//            Greeting("Android")
//        }
//    }
}