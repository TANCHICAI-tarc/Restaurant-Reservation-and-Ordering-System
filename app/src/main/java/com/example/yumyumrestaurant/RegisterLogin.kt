package com.example.yumyumrestaurant

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.yumyumrestaurant.ui.ResetViewModel
import com.example.yumyumrestaurant.ui.UserLoginViewModel
import com.example.yumyumrestaurant.ui.UserSignInViewModel
import com.example.yumyumrestaurant.ui.UserViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun Home(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tittle),
                    contentDescription = "tittle",
                    modifier = Modifier.size(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(120.dp))

            Button(
                onClick = { navController.navigate("Login") },
                modifier = Modifier
            ) {
                Text(
                    "Login",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    modifier=Modifier.width(200.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = { navController.navigate("Register") },
                modifier = Modifier
            ) {
                Text(
                    "Register",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    modifier=Modifier.width(200.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavHostController, userLoginViewModel: UserLoginViewModel){
    val viewModel: UserLoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = true) {
        navController.navigate("Home") {
            popUpTo(0) { inclusive = true }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("Home") {
                            popUpTo("Home") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.width(300.dp)
                    .height(510.dp)
                    .background(
                        Color.Blue,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Login",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.width(260.dp)
                            .height(370.dp)
                            .background(Color.White)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = { Text(text = "Email") },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Enter your email") },
                                modifier = Modifier.width(220.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            var password by remember { mutableStateOf("") }
                            var passwordVisible by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.updatePassword(it) },
                                label = { Text(text = "Password") },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Enter your password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val imageRes = if (passwordVisible)
                                        R.drawable.visibility_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                                    else
                                        R.drawable.visibility_off_24dp_1f1f1f_fill0_wght400_grad0_opsz24

                                    val description = if (passwordVisible) "Hide password" else "Show password"

                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = description,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.width(220.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Forgot password?",
                                fontSize = 15.sp,
                                color = Color.Blue,
                                modifier = Modifier.clickable {
                                    navController.navigate("ForgotPassword")
                                },
                                textDecoration = TextDecoration.Underline
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .height(40.dp)
                            ) {
                                uiState.errorMessage?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.login(
                                            onSuccess = {
                                                val isAdminEmail = uiState.email.lowercase().contains("admin")

                                                if (isAdminEmail) {
                                                    navController.navigate("AdminPage")
                                                } else {
                                                    navController.navigate("UserPage")
                                                }


                                            },
                                            onError = { /* Error handled in state */ }
                                        )
                                    },
                                    modifier = Modifier
                                ) {
                                    Text(
                                        "Login",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassword(
    navController: NavHostController,
    resetViewModel: ResetViewModel
) {
    val uiState by resetViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(400.dp)
                    .background(
                        Color.Blue,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Forgot Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .height(280.dp)
                            .background(Color.White)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { resetViewModel.updateEmail(it) },
                                label = { Text(text = "Email") },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Enter your email") },
                                modifier = Modifier.width(220.dp),
                                isError = uiState.errorMessage != null
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .height(60.dp)
                            ) {
                                if (uiState.errorMessage != null) {
                                    Text(
                                        text = uiState.errorMessage!!,
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center),
                                        maxLines = 3
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        resetViewModel.sendPasswordResetEmail(
                                            email = uiState.email,
                                            onSuccess = {
                                                navController.navigate("ResetPassword")
                                            },
                                            onError = { }
                                        )
                                    },
                                    modifier = Modifier,
                                    enabled = uiState.email.isNotBlank() && !uiState.isLoading
                                ) {
                                    Text(
                                        "Send Reset Email",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPassword(
    navController: NavHostController,
    resetViewModel: ResetViewModel
) {
    val uiState by resetViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(400.dp)
                    .background(
                        Color.Blue,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Reset Password",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .height(300.dp)
                            .background(Color.White)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { resetViewModel.updatePassword(it) },
                                label = { Text(text = "New Password") },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Enter new password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.width(220.dp),
                                supportingText = {
                                    Text("Minimum 8 characters")
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = { resetViewModel.updateConfirmPassword(it) },
                                label = { Text(text = "Confirm Password") },
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Confirm new password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.width(220.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            uiState.errorMessage?.let { errorMsg ->
                                Text(
                                    text = errorMsg,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    maxLines = 3
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        resetViewModel.resetPasswordDirectly(
                                            email = uiState.email,
                                            newPassword = uiState.password,
                                            confirmPassword = uiState.confirmPassword,
                                            onSuccess = {
                                                // Navigate to success screen
                                                navController.navigate("ResetSuccess")
                                            },
                                            onError = { errorMsg -> }
                                        )
                                    },
                                    modifier = Modifier,
                                    enabled = uiState.password.isNotBlank() &&
                                            uiState.confirmPassword.isNotBlank() &&
                                            !uiState.isLoading
                                ) {
                                    Text(
                                        "Reset Password",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResetSuccess(navController: NavHostController){

    BackHandler(enabled = true) {
        navController.navigate("Login") {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold { innerPadding ->
        SuccessReset(innerPadding = innerPadding, navController)
    }
}

@Composable
fun SuccessReset(
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Reset Password Successful!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = {
                navController.navigate("Login") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        ) {
            Text("Ok")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register(navController: NavHostController, userSignInViewModel: UserSignInViewModel){
    val viewModel: UserSignInViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.width(320.dp)
                    .height(900.dp)
                    .background(
                        Color.Blue,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Register",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Box(
                        modifier = Modifier.width(280.dp)
                            .height(760.dp)
                            .background(Color.White)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = { viewModel.updateField("name", it) },
                                label = { Text("Full Name") },
                                modifier = Modifier.width(240.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { viewModel.updateField("email", it) },
                                label = { Text("Email") },
                                placeholder = { Text("example@gmail.com") },
                                modifier = Modifier.width(240.dp),
                                isError = uiState.email.isNotBlank() && !viewModel.isValidEmail(uiState.email),
                                supportingText = {
                                    if (uiState.email.isNotBlank() && !viewModel.isValidEmail(uiState.email)) {
                                        Text("Must be example@gmail.com")
                                    }
                                }
                            )

                            Row(
                                Modifier.width(240.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.areaCode,
                                    onValueChange = { viewModel.updateField("areaCode", it.filter { it.isDigit() }.take(3)) },
                                    label = { Text("Code") },
                                    placeholder = { Text("012") },
                                    modifier = Modifier.weight(1f),
                                    isError = uiState.areaCode.isNotBlank() && uiState.areaCode.length != 3
                                )

                                Text("-", modifier = Modifier.padding(top = 16.dp))

                                OutlinedTextField(
                                    value = uiState.phoneNumber,
                                    onValueChange = { viewModel.updateField("phoneNumber", it.filter { it.isDigit() }.take(8)) },
                                    label = { Text("Number") },
                                    placeholder = { Text("3456789") },
                                    modifier = Modifier.weight(2f),
                                    isError = uiState.phoneNumber.isNotBlank() && uiState.phoneNumber.length < 7
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            var isGenderExpanded by remember { mutableStateOf(false) }
                            val genders = listOf("Male", "Female")

                            ExposedDropdownMenuBox(
                                expanded = isGenderExpanded,
                                onExpandedChange = { isGenderExpanded = it },
                                modifier = Modifier.width(240.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.gender,
                                    onValueChange = {},
                                    label = { Text("Gender") },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = isGenderExpanded
                                        )
                                    },
                                    modifier = Modifier.menuAnchor().width(260.dp),
                                    isError = uiState.gender.isBlank()
                                )

                                ExposedDropdownMenu(
                                    expanded = isGenderExpanded,
                                    onDismissRequest = { isGenderExpanded = false }
                                ) {
                                    genders.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = { Text(selectionOption) },
                                            onClick = {
                                                viewModel.updateField("gender", selectionOption)
                                                isGenderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier.width(240.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.dateOfBirth,
                                    onValueChange = {},
                                    label = { Text("Date of Birth") },
                                    placeholder = { Text("DD-MM-YYYY") },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { showDatePicker = true }
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.edit_calendar_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                                                contentDescription = "Choose date"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = uiState.dateOfBirth.isNotBlank() && !viewModel.isValidDate(uiState.dateOfBirth),
                                    supportingText = {
                                        if (uiState.dateOfBirth.isNotBlank() && !viewModel.isValidDate(uiState.dateOfBirth)) {
                                            Text("Invalid date format")
                                        }
                                    }
                                )
                            }

                            if (showDatePicker) {
                                val initialDateMillis = try {
                                    if (uiState.dateOfBirth.isNotBlank()) {
                                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                        val date = LocalDate.parse(uiState.dateOfBirth, formatter)
                                        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    } else {
                                        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    }
                                } catch (e: Exception) {
                                    LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                }

                                val maxDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val datePickerState = rememberDatePickerState(
                                    initialSelectedDateMillis = initialDateMillis,
                                    yearRange = IntRange(0, LocalDate.now().year),
                                    selectableDates = object : SelectableDates {
                                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                            return utcTimeMillis <= maxDate
                                        }
                                    }
                                )

                                DatePickerDialog(
                                    onDismissRequest = { showDatePicker = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                    val selectedDate = Instant
                                                        .ofEpochMilli(millis)
                                                        .atZone(ZoneId.systemDefault())
                                                        .toLocalDate()

                                                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                                    val formattedDate = selectedDate.format(formatter)
                                                    viewModel.updateField("dateOfBirth", formattedDate)
                                                }
                                                showDatePicker = false
                                            }
                                        ) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showDatePicker = false }
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    DatePicker(
                                        state = datePickerState
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.updateField("password", it) },
                                label = { Text("Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val imageRes = if (passwordVisible)
                                        R.drawable.visibility_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                                    else
                                        R.drawable.visibility_off_24dp_1f1f1f_fill0_wght400_grad0_opsz24

                                    val description = if (passwordVisible) "Hide password" else "Show password"

                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = description,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.width(240.dp),
                                isError = uiState.password.isNotBlank() && uiState.password.length < 8,
                                supportingText = {
                                    if (uiState.password.isNotBlank() && uiState.password.length < 8) {
                                        Text("Min 8 characters")
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = { viewModel.updateField("confirmPassword", it) },
                                label = { Text("Confirm Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val imageRes = if (passwordVisible)
                                        R.drawable.visibility_24dp_1f1f1f_fill0_wght400_grad0_opsz24
                                    else
                                        R.drawable.visibility_off_24dp_1f1f1f_fill0_wght400_grad0_opsz24

                                    val description = if (passwordVisible) "Hide password" else "Show password"

                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Image(
                                            painter = painterResource(id = imageRes),
                                            contentDescription = description,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.width(240.dp),
                                isError = uiState.confirmPassword.isNotBlank() && uiState.password != uiState.confirmPassword,
                                supportingText = {
                                    if (uiState.confirmPassword.isNotBlank() && uiState.password != uiState.confirmPassword) {
                                        Text("Passwords don't match")
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .height(40.dp)
                            ) {
                                uiState.errorMessage?.let {
                                    Text(
                                        text = it,
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.signUp(
                                            onSuccess = {
                                                navController.navigate("SuccessRegister") {
                                                    popUpTo(0)
                                                }
                                            },
                                            onError = { /* Error handled in state */ }
                                        )
                                    },
                                    modifier = Modifier
                                ) {
                                    Text(
                                        "Sign Up",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessRegister(navController: NavHostController) {

    BackHandler(enabled = true) {
        navController.navigate("Home") {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold { innerPadding ->
        RegisterSuccessful(innerPadding = innerPadding, navController)
    }
}

@Composable
fun RegisterSuccessful(
    innerPadding: PaddingValues,
    navController: NavHostController
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Register Successful!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = {
                navController.navigate("Home") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        ) {
            Text("Ok")
        }
    }
}