package com.fm.fmmedia.ui.login

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.fmmedia.R
import com.fm.fmmedia.api.request.Register
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.compose.textInput
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.util.isValidEmail
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.LoginViewModel
import kotlinx.coroutines.delay


/**
 * 用户注册
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun registerScreen(
    loginViewModel: LoginViewModel,
    accessTokenViewModel: AccessTokenViewModel,
    onHome: () -> Unit = {},
    onBack: () -> Unit = {}
) {

    var email by remember { mutableStateOf("") }
    var isEmailError by remember {
        mutableStateOf(false)
    }
    var password by remember { mutableStateOf("") }
    var isPasswordError by remember {
        mutableStateOf(false)
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var verificationCode by remember {
        mutableStateOf("")
    }
    var isVerificationCodeError by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }
    var isSendEmail by remember {
        mutableStateOf(false)
    }
    val errorMsg by loginViewModel.errorMsg.observeAsState()
    val errorCode by loginViewModel.errorCode.observeAsState()
    val registerEmailResult by loginViewModel.registerEmail.observeAsState()
    val registerResponse by loginViewModel.registerResponse.observeAsState()
    var toast: Toast? = null
    val context = LocalContext.current
    LaunchedEffect(errorCode) {
        isLoading = false
        isSendEmail = false
        if (errorCode != 0) {
            toast?.cancel()
            toast = Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    LaunchedEffect(registerEmailResult){
        isLoading = false
    }

    LaunchedEffect(registerResponse){
        registerResponse?.let {loginResponse->
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



    val defaultSecond = 60
    var countDown by remember {
        mutableStateOf(defaultSecond)
    }
    LaunchedEffect(errorCode) {
        isLoading = false
        if (errorCode != 0) {
            toast?.cancel()
            toast = Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT)
            toast?.show()
        }
        loginViewModel.clear()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)

            if (isSendEmail) {
                countDown--
            }
            if(countDown == 0) {
                isSendEmail = false
                countDown = defaultSecond
            }
        }
    }

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
                        text = stringResource(id = R.string.register_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 10.dp)
                ) {
                    textInput(
                        label = stringResource(id = R.string.login_label),
                        imageVector = Icons.Default.Person,
                        placeholder = stringResource(
                            id = R.string.register_email
                        ),
                        isError = isEmailError,
                        supporting = stringResource(id = R.string.register_email_supporting),
                        onValueChange = { text ->
                            email = text
                            if(isEmailError && isValidEmail(email.trim())){
                                isEmailError = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 10.dp)
                ) {
                    textInput(label = stringResource(id = R.string.password_label),
                        imageVector = Icons.Default.Lock,
                        isPassword = true,
                        placeholder = stringResource(
                            id = R.string.password_placeholder
                        ),
                        isError = isPasswordError,
                        supporting = stringResource(id = R.string.register_password_supporting),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        onValueChange = { text ->
                            password = text
                        }
                    )
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 10.dp)
                ) {
                    textInput(
                        label = stringResource(id = R.string.confirm_password_label),
                        imageVector = Icons.Default.Lock,
                        isPassword = true,
                        placeholder = stringResource(
                            id = R.string.password_placeholder
                        ),
                        isError = isPasswordError,
                        supporting = stringResource(id = R.string.register_password_supporting),
                        onValueChange = { text -> confirmPassword = text },
                        modifier = Modifier
                            .fillMaxWidth()
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
                            supporting = stringResource(id = R.string.register_verification_code_empty),
                            isError = isVerificationCodeError,
                            onValueChange = { text -> verificationCode = text },
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        )
                        Button(
                            onClick = {
                                if(isSendEmail) return@Button
                                if(isValidEmail(email = email.trim())){
                                    isLoading = true
                                    isSendEmail = true
                                    loginViewModel.registerEmail(email)
                                } else {
                                    Log.e("测试", email)
                                    isEmailError = true
                                }
                            },
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
                                text = if(isSendEmail) countDown.toString() else stringResource(id = R.string.register_request_verification_code),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if(email.isEmpty() || !isValidEmail(email)){
                            isEmailError = true
                            return@Button
                        }
                        if(password.isEmpty() || confirmPassword.isEmpty() || password != confirmPassword){
                            isPasswordError = true
                            return@Button
                        }
                        isPasswordError = false
                        if(verificationCode.isEmpty()){
                            isVerificationCodeError = true
                            return@Button
                        }
                        isLoading = true
                        loginViewModel.register(
                            Register(
                                email = email,
                                password = password,
                                verificationCode = verificationCode
                            )
                        )
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
                        text = stringResource(id = R.string.register_button_text),
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
