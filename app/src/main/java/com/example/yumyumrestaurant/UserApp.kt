package com.example.yumyumrestaurant

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerState
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModelFactory
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.ReservationTableData.ReservationTableRepository
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableRepository
import com.example.yumyumrestaurant.shareFilterScreen.SharedFilterViewModel
import com.example.yumyumrestaurant.ui.AboutViewModel
import com.example.yumyumrestaurant.ui.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun UserHome(navController: NavHostController, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Welcome to " +
                    "Yum Yum" +
                    "Restaurant!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(16.dp)
                .width(220.dp)
                .background(color = Color.Blue)
        ){

        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(16.dp)
                .width(220.dp)
                .background(color = Color.Yellow)
        ){
            Text(
                text = "Reservation Rules:\n" +
                        "- Restaurant open: 9:00 AM to 10:00 PM\n" +
                        "- Reservations must be made at least 2 hours in advance\n" +
                        "- Maximum reservation duration: 5 hours",
                modifier = Modifier.padding(16.dp),
                color = Color.Black
            )

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Reservation(
    userViewModel: UserViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope,
    openDrawer: () -> Unit,
    onNavigateToViewReservation: () -> Unit,
    sharedFilterViewModel:SharedFilterViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val reservationNavController = rememberNavController()
        val menuViewModel: MenuViewModel = viewModel()
        val orderViewModel: OrderViewModel = viewModel()



        val context = LocalContext.current
        val application = context.applicationContext as Application


        val tableViewModel: TableViewModel = viewModel()
        val database = Reservation_TableDatabase.getReservationTableDatabase(application)
        val reservationTableRepository = ReservationTableRepository(database.reservationTableDao())
        val reservationRepository = ReservationRepository(database.reservationDao())
        val tableRepository = TableRepository(database.tableDao())
        val reservationViewModel: ReservationViewModel = viewModel()


        val reservationTableViewModel: ReservationTableViewModel = viewModel(
            factory = ReservationTableViewModelFactory(
                tableViewModel,
                reservationViewModel,
                reservationTableRepository,
                reservationRepository = reservationRepository,
                tableRepository = tableRepository,
            )
        )

        TableReservationNavigation(
            reservationTableViewModel = reservationTableViewModel,
            menuViewModel = menuViewModel,
            orderViewModel = orderViewModel,
            userViewModel= userViewModel,
            navController = reservationNavController,
            modifier = Modifier.fillMaxSize(),
            isFirstScreenDrawer = true,
            drawerState = drawerState,
            scope = scope,
            openDrawer = openDrawer,
            onNavigateToViewReservation,
            sharedFilterViewModel
        )
    }
}
@Composable
fun ViewReservation(navController: NavHostController, userViewModel: UserViewModel, sharedFilterViewModel: SharedFilterViewModel, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()

    ) {
        UserViewReservationScreen(navController,userViewModel, sharedFilterViewModel = sharedFilterViewModel, modifier = modifier,onViewDetail = { resId ->

            navController.navigate("reservation_detail/$resId")
        })

    }
}

@Composable
fun MainUserProfile(navController: NavHostController, userViewModel: UserViewModel, onLogout: () -> Unit) {
    val userData by userViewModel.userData.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val user = userData!!

        Box(
            modifier = Modifier.width(300.dp)
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
                    "Profile",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(30.dp))
                Box(
                    modifier = Modifier.width(260.dp)
                        .height(280.dp)
                        .background(Color.White)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UserId:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = user.userId,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Name:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = user.name,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Email:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = user.email,
                                fontSize = 16.sp
                            )
                        }


                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Phone:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = user.phoneNum,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gender:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = user.gender,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Date of Birth:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = user.dateOfBirth,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        IconButton(
                            onClick = { navController.navigate("UserEdit") },
                            modifier = Modifier
                                .padding(8.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_square_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                                contentDescription = "UserEdit",
                                tint = Color.Unspecified,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEdit(navController: NavHostController, userViewModel: UserViewModel, onLogout: () -> Unit) {
    val userData by userViewModel.userData.collectAsState()
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        userViewModel.resetEditFields()
                        navController.popBackStack()
                    }) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val user = userData!!

            Box(
                modifier = Modifier.width(320.dp)
                    .height(1000.dp)
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
                        "Edit Profile",
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Box(
                        modifier = Modifier.width(280.dp)
                            .height(860.dp)
                            .background(Color.White)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = user.editUserName,
                                onValueChange = userViewModel::updateEditName,
                                label = { Text("Name") },
                                modifier = Modifier.width(240.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = user.editUserPhoneNum,
                                onValueChange = userViewModel::updateEditPhoneNumber,
                                label = { Text("Phone Number") },
                                modifier = Modifier.width(240.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            var isGenderExpanded by remember { mutableStateOf(false) }
                            val genders = listOf("Male", "Female")

                            ExposedDropdownMenuBox(
                                expanded = isGenderExpanded,
                                onExpandedChange = { isGenderExpanded = it },
                                modifier = Modifier.width(240.dp)
                            ) {
                                OutlinedTextField(
                                    value = user.editUserGender,
                                    onValueChange = {},
                                    label = { Text("Gender") },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = isGenderExpanded
                                        )
                                    },
                                    modifier = Modifier.menuAnchor().width(260.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = isGenderExpanded,
                                    onDismissRequest = { isGenderExpanded = false }
                                ) {
                                    genders.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = { Text(selectionOption) },
                                            onClick = {
                                                userViewModel.updateEditGender(selectionOption)
                                                isGenderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            var showDatePicker by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier.width(240.dp)
                            ) {
                                OutlinedTextField(
                                    value = user.editUserDateOfBirth,
                                    onValueChange = userViewModel::updateEditDateOfBirth,
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
                                    isError = user.dateOfBirth.isNotBlank() && !userViewModel.isValidDate(
                                        user.editUserDateOfBirth
                                    ),
                                    supportingText = {
                                        if (user.dateOfBirth.isNotBlank() && !userViewModel.isValidDate(
                                                user.editUserDateOfBirth
                                            )
                                        ) {
                                            Text("Invalid date format")
                                        }
                                    }
                                )
                            }

                            if (showDatePicker) {
                                val initialDateMillis = try {
                                    val currentDate = user.editUserDateOfBirth.ifBlank {
                                        user.dateOfBirth
                                    }

                                    if (currentDate.isNotBlank()) {
                                        val formatter =
                                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                        val date = LocalDate.parse(currentDate, formatter)
                                        date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                                            .toEpochMilli()
                                    } else {
                                        LocalDate.now()
                                            .minusYears(18)
                                            .atStartOfDay(ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli()
                                    }
                                } catch (e: Exception) {
                                    LocalDate.now()
                                        .minusYears(18)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli()
                                }

                                val maxDate =
                                    LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                                        .toInstant().toEpochMilli()
                                val datePickerState = rememberDatePickerState(
                                    initialSelectedDateMillis = initialDateMillis,
                                    yearRange = IntRange(1900, LocalDate.now().year),
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

                                                    val formatter =
                                                        DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                                    val formattedDate =
                                                        selectedDate.format(formatter)
                                                    userViewModel.updateEditDateOfBirth(
                                                        formattedDate
                                                    )
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
                                    DatePicker(state = datePickerState)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Change Password",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = user.editUserCurrentPassword,
                                onValueChange = userViewModel::updateEditCurrentPassword,
                                label = { Text("Current Password") },
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
                                modifier = Modifier.width(240.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = user.editUserNewPassword,
                                onValueChange = userViewModel::updateEditNewPassword,
                                label = { Text("New Password") },
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
                                modifier = Modifier.width(240.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = user.editUserConfirmPassword,
                                onValueChange = userViewModel::updateEditConfirmPassword,
                                label = { Text("Confirm New Password") },
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
                                modifier = Modifier.width(240.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .width(260.dp)
                                    .height(60.dp)
                            ) {
                                if (user.editUserErrorMessage != null) {
                                    Text(
                                        text = user.editUserErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth()
                                    )
                                } else if (user.editUserIsSuccess) {
                                    Text(
                                        text = "Profile updated successfully!",
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    userViewModel.saveUserChanges()
                                },
                                modifier = Modifier
                                    .width(240.dp)
                                    .height(50.dp),
                                enabled = !user.editUserIsLoading
                            ) {
                                if (user.editUserIsLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        text = "Save Changes",
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    userViewModel.resetEditFields()
                                    navController.popBackStack()
                                },
                                modifier = Modifier
                                    .width(240.dp)
                                    .height(50.dp),
                                enabled = !user.editUserIsLoading
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAbout(navController: NavHostController, modifier: Modifier, aboutViewModel: AboutViewModel) {
    val uiState by aboutViewModel.uiState.collectAsState()
    val loadingState by aboutViewModel.loadingState.collectAsState()

    when (loadingState) {
        is AboutViewModel.LoadingState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AboutViewModel.LoadingState.Error -> {
            val errorMessage = (loadingState as AboutViewModel.LoadingState.Error).message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $errorMessage", color = Color.Red)
            }
        }
        is AboutViewModel.LoadingState.Success -> {
            val about = uiState ?: return

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = about.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 40.sp,
                        lineHeight = 52.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.padding(10.dp))

                Text(
                    text = about.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccount(
    navController: NavHostController,
    modifier: Modifier,
    userViewModel: UserViewModel
) {
    val isLoading = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Delete Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Warning: This action cannot be undone. All your data will be permanently deleted.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading.value) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading.value = true
                    deleteUserAccount(userViewModel, isLoading)
                }
            ) {
                Text("Delete My Account")
            }
        }
    }
}

private fun deleteUserAccount(
    userViewModel: UserViewModel,
    isLoading: MutableState<Boolean>
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    if (currentUser != null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Delete user data from Firestore
                db.collection("Users").document(currentUser.uid).delete().await()

                // 2. Delete authentication account
                currentUser.delete().await()

                // 3. Trigger navigation to state_choice
                CoroutineScope(Dispatchers.Main).launch {
                    userViewModel.triggerStateChoiceNavigation()
                }
            } catch (e: Exception) {
                isLoading.value = false
            }
        }
    } else {
        isLoading.value = false
        userViewModel.triggerStateChoiceNavigation()
    }
}