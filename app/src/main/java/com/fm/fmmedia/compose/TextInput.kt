package com.fm.fmmedia.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.fmmedia.ui.theme.FmMediaTheme

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
    isError: Boolean = false,
    onValueChange: (text: String) -> Unit,
    modifier: Modifier = Modifier
) {

    var text by remember {
        mutableStateOf("")
    }

    val currentModifier = if(isError)  modifier.height(80.dp) else modifier.height(60.dp)

    TextField(
        modifier = currentModifier
            .clip(shape = CircleShape.copy(all = CornerSize(8.dp)))
            .background(FmMediaTheme.colors.uiFloated),
        value = text,
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        supportingText = {
            if(isError) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = supporting,
                    )
                }
            }
        },
        isError = isError,
        onValueChange = {
            onValueChange(it)
            text = it
        },
        textStyle = TextStyle(color = Color.Black, fontSize = 12.sp), // 设置字体颜色
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        //开始和结束时的图标
        leadingIcon = { Icon(imageVector = imageVector, contentDescription = null) },

        label = { Text(label, color = Color.Black, fontSize = 12.sp) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = FmMediaTheme.colors.iconPrimary, // 设置焦点时的边框颜色
//            unfocusedBorderColor = FmMediaTheme.colors.iconPrimary, // 设置非焦点时的边框颜色
            cursorColor = FmMediaTheme.colors.iconPrimary,
            errorCursorColor = FmMediaTheme.colors.iconPrimary,
        )
    )
}

