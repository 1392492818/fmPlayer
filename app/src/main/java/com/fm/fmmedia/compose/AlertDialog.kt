package com.fm.fmmedia.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fm.fmmedia.R
import com.fm.fmmedia.ui.theme.FmMediaTheme


@Composable
fun AlertDialog(showDialogDefault:Boolean,
                title:String = stringResource(id = R.string.alert_dialog_title),
                body: String = stringResource(id = R.string.alert_dialog_body),
                onConfirm: (isConfirm:Boolean)->Unit = {}) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(showDialogDefault) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onConfirm(false)

                },
                title = {
                    Text(text = title)
                },
                text = {
                    Text(text = body)
                },
                confirmButton = {
                    Button(onClick = {
                        // 处理确认按钮点击事件
                        showDialog = false
                        onConfirm(true)
                        // 在此处添加你的处理逻辑
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = FmMediaTheme.colors.iconPrimary, // 按钮的背景颜色
                        contentColor = Color.White // 按钮的内容颜色（文本和图标）
                    )) {
                        Text(text = stringResource(id = R.string.alert_dialog_confirm))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        // 处理取消按钮点击事件
                        showDialog = false
                        onConfirm(false)

                        // 在此处添加你的处理逻辑
                    },colors = ButtonDefaults.buttonColors(
                        containerColor = FmMediaTheme.colors.iconPrimary, // 按钮的背景颜色
                        contentColor = Color.White // 按钮的内容颜色（文本和图标）
                    )) {
                        Text(text = stringResource(id = R.string.alert_dialog_cancel))
                    }
                }
            )
        }
    }
}