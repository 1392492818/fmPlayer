package com.fm.fmmedia.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRequestScreen(
    onPermissionDenied: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera permission is required to use the camera feature.")

        Spacer(modifier = Modifier.height(16.dp))

        // 显示请求权限的按钮
        Button(
            onClick = {
                // 请求权限
                onPermissionDenied()
            }
        ) {
            Text("Request Permission")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 显示打开应用设置的按钮
        TextButton(
            onClick = {
                // 打开应用设置页面
                onOpenSettings()
            }
        ) {
            Text("Open Settings")
        }
    }
}
