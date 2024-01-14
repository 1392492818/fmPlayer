package com.fm.fmmedia.ui.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.fmmedia.R
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.viewmodel.LoginViewModel


/**
@param placeholder 输入提示
@param supporting 底部名字显示
@param label  显示输入数据内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textInput(
    placeholder: String = "",
    supporting: String = "",
    label: String = "",
    isPassword: Boolean = false,
    imageVector: ImageVector = Icons.Default.Person,
    onValueChange: (text: String) -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
) {

    var text by remember {
        mutableStateOf("")
    }
//    var password by remember{
//        mutableStateOf("")
//    }
    TextField(
        modifier = modifier
//            .border(
//                width = 2.dp,
//                color = Color.Green,
//                shape = CircleShape.copy(all = CornerSize(16.dp))
//            )
            .clip(shape = CircleShape.copy(all = CornerSize(16.dp)))
            .background(Color.Yellow),
        value = text,
        singleLine = true,
        //默认为false，设置为true时,label和supportText显示为红色
//            isError = true,
        //获取输入的内容

        visualTransformation = if (isPassword) PasswordVisualTransformation()  else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions(keyboardType = KeyboardType.Text),
        onValueChange = {
            onValueChange(it)
            text = it
        },
        placeholder = { Text(placeholder) },
        //开始和结束时的图标
        leadingIcon = { Icon(imageVector = imageVector, contentDescription = null) },
//        trailingIcon = {
//            Icon(
//                imageVector = Icons.Filled.Notifications,
//                contentDescription = null
//            )
//        },
        //在输入框下方显示的文字
//        supportingText = { Text(supporting) },
        //在图标后面显示的文字
        label = { Text(label) },
    )
}


@Composable
fun forgetPassword() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center  // 垂直居中
    ) {
        Text(text = stringResource(id = R.string.forget_title), fontSize = 50.sp)
        textInput(label = stringResource(id = R.string.login_label),
            imageVector = Icons.Default.Person,
            placeholder = stringResource(
                id = R.string.login_placeholder
            ),
            onValueChange = { text -> Log.e("logininput", text) })
        textInput(label = stringResource(id = R.string.password_label),
            imageVector = Icons.Default.Lock,
            isPassword = true,
            placeholder = stringResource(
                id = R.string.password_placeholder
            ),
            onValueChange = { text -> Log.e("logininput", text) })


        Row() {
            textInput(
                label = stringResource(id = R.string.register_verification_code),
                imageVector = Icons.Default.MailOutline,
                placeholder = stringResource(
                    id = R.string.register_verification_placeholder
                ),
                onValueChange = { text -> Log.e("logininput", text) },
                modifier = Modifier
                    .weight(2f)
                    .padding(16.dp)
            )
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 80.dp)
                    .align(CenterVertically)
                    .padding(16.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.register_request_verification_code),
                    fontSize = 10.sp
                )
            }
        }
        Button(
            onClick = { /*TODO*/ }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = PaddingValues(top = 15.dp, bottom = 15.dp)
        ) {
            Text(
                text = stringResource(id = R.string.forget_button_text),
                fontSize = 18.sp
            )
        }
    }
}

/**
 * 用户注册
 */
@Composable
fun registerScreen() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center  // 垂直居中
    ) {
        Text(text = stringResource(id = R.string.register_title), fontSize = 50.sp)
        textInput(label = stringResource(id = R.string.login_label),
            imageVector = Icons.Default.Person,
            placeholder = stringResource(
                id = R.string.login_placeholder
            ),
            onValueChange = { text -> Log.e("logininput", text) })
        textInput(label = stringResource(id = R.string.password_label),
            imageVector = Icons.Default.Lock,
            isPassword = true,
            placeholder = stringResource(
                id = R.string.password_placeholder
            ),
            onValueChange = { text -> Log.e("logininput", text) })
        textInput(label = stringResource(id = R.string.confirm_password_label),
            imageVector = Icons.Default.Lock,
            isPassword = true,
            placeholder = stringResource(
                id = R.string.password_placeholder
            ),
            onValueChange = { text -> Log.e("logininput", text) })

        Row() {
            textInput(
                label = stringResource(id = R.string.register_verification_code),
                imageVector = Icons.Default.MailOutline,
                placeholder = stringResource(
                    id = R.string.register_verification_placeholder
                ),
                onValueChange = { text -> Log.e("logininput", text) },
                modifier = Modifier
                    .weight(2f)
                    .padding(16.dp)
            )
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 80.dp)
                    .align(CenterVertically)
                    .padding(16.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.register_request_verification_code),
                    fontSize = 10.sp
                )
            }
        }
        Button(
            onClick = { /*TODO*/ }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = PaddingValues(top = 15.dp, bottom = 15.dp)
        ) {
            Text(
                text = stringResource(id = R.string.register_button_text),
                fontSize = 18.sp
            )
        }
    }
}

/**
 * 用户登录
 */
@Composable
fun loginScreen(onForgetPassword: () -> Unit, onRegister: () -> Unit, onHome: () -> Unit) {
    var account: String = "1392492818@qq.com"
    var password: String = "123456789"
    val loginViewModel: LoginViewModel = LoginViewModel()
    val loginLiveData by loginViewModel.loginLiveData.observeAsState()
    LaunchedEffect(loginLiveData) {
        if (loginLiveData != null) {
            onHome()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center  // 垂直居中
    ) {
        Text(text = stringResource(id = R.string.login_title), fontSize = 50.sp)
        textInput(label = stringResource(id = R.string.login_label),
            imageVector = Icons.Default.Person,
            placeholder = stringResource(
                id = R.string.login_placeholder
            ),
            onValueChange = { text -> account = text })
        textInput(label = stringResource(id = R.string.password_label),
            imageVector = Icons.Default.Lock,
            isPassword = true,
            placeholder = stringResource(
                id = R.string.password_placeholder
            ),
            onValueChange = { text ->
                Log.e("测试", text)
                password = text
            })
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                loginViewModel.login(account, password)
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = PaddingValues(top = 15.dp, bottom = 15.dp)
        ) {
            Text(
                text = stringResource(id = R.string.login_button_text),
                fontSize = 18.sp
            )
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