package com.example.feature.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanButton
import com.example.designsystem.theme.PrimaryTeal
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    var isLogin by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.signUpEvent.collect { success ->
            if (success) {
                isLogin = true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect { success ->
            if (success) {
                onAuthSuccess()
            } else {
                snackbarHostState.showSnackbar("رقم الهاتف غير مسجل")
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                width = 40.dp,
                height = 4.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        },
        containerColor = Color.White
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.White,
                modifier = Modifier.heightIn(max = 600.dp)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLogin) {
                        LoginContent(
                            onSignUpClick = { isLogin = false },
                            onLoginClick = { phone -> viewModel.login(phone) }
                        )
                    } else {
                        SignUpContent(
                            onLoginClick = { isLogin = true },
                            onSignUpClick = { fName, lName, phone ->
                                viewModel.signUp(fName, lName, phone)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginContent(
    onSignUpClick: () -> Unit,
    onLoginClick: (String) -> Unit
) {
    var phone by remember { mutableStateOf("") }

    Text(
        text = "تسجيل الدخول للمتابعة",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = PrimaryTeal,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = "لتتمكن من إضافة تبرعك أو طلبك بأمان",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
    )

    AuthTextField(
        value = phone,
        onValueChange = { phone = it },
        placeholder = "رقم الهاتف",
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )

    Spacer(modifier = Modifier.height(32.dp))

    IhsanButton(
        onClick = { onLoginClick(phone) },
        modifier = Modifier.fillMaxWidth(),
        containerColor = PrimaryTeal,
        enabled = phone.isNotBlank()
    ) {
        Text("تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.height(24.dp))
    OrDivider()
    Spacer(modifier = Modifier.height(24.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("ليس لديك حساب؟ ", color = Color.Gray)
        Text(
            "إنشاء حساب جديد",
            color = PrimaryTeal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onSignUpClick() }
        )
    }
}

@Composable
fun SignUpContent(
    onLoginClick: () -> Unit,
    onSignUpClick: (String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Text(
        text = "إنشاء حساب جديد",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = PrimaryTeal,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = "أنشئ حسابك بسرعة لمتابعة التبرع أو طلب المساعدة بأمان",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
    )

    AuthTextField(
        value = firstName,
        onValueChange = { firstName = it },
        placeholder = "الاسم الأول",
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }
    )
    Spacer(modifier = Modifier.height(16.dp))
    AuthTextField(
        value = lastName,
        onValueChange = { lastName = it },
        placeholder = "الكنية",
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }
    )
    Spacer(modifier = Modifier.height(16.dp))
    AuthTextField(
        value = phone,
        onValueChange = { phone = it },
        placeholder = "رقم الهاتف",
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )

    Spacer(modifier = Modifier.height(32.dp))

    IhsanButton(
        onClick = { onSignUpClick(firstName, lastName, phone) },
        modifier = Modifier.fillMaxWidth(),
        containerColor = PrimaryTeal,
        enabled = firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()
    ) {
        Text("إنشاء الحساب والاشتراك", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.height(24.dp))
    OrDivider()
    Spacer(modifier = Modifier.height(24.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("لديك حساب بالفعل؟ ", color = Color.Gray)
        Text(
            "تسجيل الدخول",
            color = PrimaryTeal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onLoginClick() }
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedBorderColor = PrimaryTeal,
            unfocusedContainerColor = Color(0xFFF9F9F9),
            focusedContainerColor = Color(0xFFF9F9F9)
        ),
        singleLine = true
    )
}

@Composable
fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
        Text(
            "أو",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray,
            fontSize = 14.sp
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}
