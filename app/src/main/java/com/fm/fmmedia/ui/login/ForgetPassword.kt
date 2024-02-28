package com.fm.fmmedia.ui.login

import android.util.Log
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.fmmedia.R
import com.fm.fmmedia.compose.textInput
import com.fm.fmmedia.ui.theme.FmMediaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun forgetPassword(onBack: ()->Unit = {}) {
    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .padding(15.dp, paddingValues.calculateTopPadding())
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {

                Icon(imageVector = Icons.Default.Close, modifier = Modifier.clickable {
                    onBack()
                }, contentDescription = null)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.forget_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 10.dp)
                ) {
                    textInput(label = stringResource(id = R.string.login_label),
                        imageVector = Icons.Default.Person,
                        placeholder = stringResource(
                            id = R.string.login_placeholder
                        ),
                        onValueChange = { text -> Log.e("logininput", text)
                        },
                        modifier = Modifier.fillMaxWidth()
                            .height(60.dp)
                            .padding(5.dp)
                    )
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 10.dp)
                ) {
                    Row {
                        textInput(
                            label = stringResource(id = R.string.register_verification_code),
                            imageVector = Icons.Default.MailOutline,
                            placeholder = stringResource(
                                id = R.string.register_verification_placeholder
                            ),
                            onValueChange = { text -> Log.e("logininput", text) },
                            modifier = Modifier
                                .weight(1f).padding(5.dp)
                        )
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(10.dp)
                                .align(Alignment.CenterVertically),
                            shape = RoundedCornerShape(5.dp), // 圆角形状，8dp的圆角
                            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FmMediaTheme.colors.iconPrimary, // 按钮的背景颜色
                                contentColor = Color.White // 按钮的内容颜色（文本和图标）
                            )
                        ) {
                            Text(
                                text = stringResource(id = R.string.register_request_verification_code),
                                fontSize = 10.sp
                            )
                        }
                    }

                }
                Button(
                    onClick = {
//                        isLoading = true
//                        loginViewModel.login(account, password)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(5.dp), // 圆角形状，8dp的圆角
                    contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FmMediaTheme.colors.iconPrimary, // 按钮的背景颜色
                        contentColor = Color.White // 按钮的内容颜色（文本和图标）
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.forget_button_text),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

}
