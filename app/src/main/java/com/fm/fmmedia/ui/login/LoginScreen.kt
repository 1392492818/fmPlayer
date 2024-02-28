package com.fm.fmmedia.ui.login

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.fmmedia.R
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.compose.textInput
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.repository.LoginRepository
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.LoginViewModel


/**
 * 用户登录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun loginScreen(
    onForgetPassword: () -> Unit,
    onRegister: () -> Unit,
    onHome: () -> Unit,
    onBack: () -> Unit,
    loginViewModel: LoginViewModel,
    accessTokenViewModel: AccessTokenViewModel
) {
    var account by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val loginLiveData by loginViewModel.loginLiveData.observeAsState()
    val errorCode by loginViewModel.errorCode.observeAsState()
    val errorMsg by loginViewModel.errorMsg.observeAsState()
    var isLoading by remember {
        mutableStateOf(false)
    }
    var toast: Toast? = null
    val context = LocalContext.current

    LaunchedEffect(loginLiveData) {
        loginLiveData?.let { loginResponse ->
            isLoading = false
            val accessToken: AccessToken = AccessToken(
                id = loginResponse.id,
                accessToken = loginResponse.accessToken,
                expiresTime = loginResponse.expiresTime.toLong(),
                refreshToken = loginResponse.refreshToken,
                userId = loginResponse.userId,
                status = loginResponse.status
            );
            accessTokenViewModel.insert(accessToken)
            loginViewModel.clear()
            onHome()
        }
    }


    LaunchedEffect(errorCode) {
        isLoading = false
        if (errorCode != 0) {
            toast?.cancel()
            toast = Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }


    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(15.dp, paddingValues.calculateTopPadding())
                    .fillMaxHeight()
                    .fillMaxWidth(),
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
                        text = stringResource(id = R.string.login_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier.padding(0.dp, 10.dp)) {
                    textInput(
                        label = stringResource(id = R.string.login_label),
                        imageVector = Icons.Default.Person,
                        placeholder = stringResource(
                            id = R.string.login_placeholder
                        ),
                        onValueChange = { text -> account = text },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(5.dp)
                    )
                }

                Box(modifier = Modifier.padding(0.dp, 10.dp)) {
                    textInput(
                        label = stringResource(id = R.string.password_label),
                        imageVector = Icons.Default.Lock,
                        isPassword = true,
                        placeholder = stringResource(
                            id = R.string.password_placeholder
                        ),
                        onValueChange = { text ->
                            Log.e("测试", text)
                            password = text
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(5.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onForgetPassword() },
                        text = stringResource(id = R.string.forget_password)
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRegister() },
                        text = stringResource(id = R.string.register),
                        textAlign = TextAlign.End
                    )
                }
                Button(
                    onClick = {
                        isLoading = true
                        loginViewModel.login(account, password)
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
                        text = stringResource(id = R.string.login_button_text),
                        fontSize = 16.sp
                    )
                }
            }


            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { }, contentAlignment = Alignment.Center
                ) {
                    loading()
                }
            }
        }

    }


}


@Composable
@Preview
fun username() {
    FmMediaTheme {
        Surface {
            //带下划线的输入框,不过我在外层加了一层边框
            forgetPassword()
        }
    }
}