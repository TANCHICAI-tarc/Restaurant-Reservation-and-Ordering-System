package com.example.yumyumrestaurant.ui
import java.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.Abouts
import com.example.yumyumrestaurant.Account
import com.example.yumyumrestaurant.UserSession
import com.example.yumyumrestaurant.data.Converters
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


import com.google.firebase.Timestamp


class UserViewModel : ViewModel() {
    private val _navigateToStateChoice = MutableStateFlow(false)
    val navigateToStateChoice: StateFlow<Boolean> = _navigateToStateChoice.asStateFlow()

    private val _userData = MutableStateFlow<Account?>(null)
    val userData: StateFlow<Account?> = _userData.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Loading)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                clearUserData()
            } else {
                loadUserData()
            }
        }
        loadUserData()
    }

    fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    _loadingState.value = LoadingState.Loading
                    val userDoc = db.collection("Users").document(currentUser.uid).get().await()

                    if (userDoc.exists()) {
                        val user = Account(
                            userId = userDoc.getString("userId") ?: currentUser.uid,
                            name = userDoc.getString("name") ?: currentUser.displayName ?: "",
                            email = userDoc.getString("email") ?: currentUser.email ?: "",
                            password = userDoc.getString("password") ?: "",
                            phoneNum = userDoc.getString("phoneNum") ?: "",
                            gender = userDoc.getString("gender") ?: "",
                            dateOfBirth = userDoc.getString("dateOfBirth") ?: "",
                            confirmPassword = "",
                            areaCode = "",
                            phoneNumber = "",
                            isLoading = false,
                            errorMessage = null,
                            editUserName = userDoc.getString("name") ?: currentUser.displayName ?: "",
                            editUserPhoneNum = userDoc.getString("phoneNum") ?: "",
                            editUserGender = userDoc.getString("gender") ?: "",
                            editUserDateOfBirth = userDoc.getString("dateOfBirth") ?: "",
                            editUserCurrentPassword = "",
                            editUserNewPassword = "",
                            editUserConfirmPassword = "",
                            editUserIsLoading = false,
                            editUserErrorMessage = null,
                            editUserIsSuccess = false
                        )
                        _userData.value = user
                        _loadingState.value = LoadingState.Success(user)
                    } else {
                        _loadingState.value = LoadingState.Error("User data not found")
                    }
                } catch (e: Exception) {
                    _loadingState.value = LoadingState.Error("Failed to load user data: ${e.message}")
                }
            }
        } else {
            _loadingState.value = LoadingState.Error("No user logged in")
        }
    }

    fun updateEditName(name: String) {
        _userData.value = _userData.value?.copy(editUserName = name)
    }

    fun updateEditPhoneNumber(phoneNum: String) {
        _userData.value = _userData.value?.copy(editUserPhoneNum = phoneNum)
    }

    fun updateEditGender(gender: String) {
        _userData.value = _userData.value?.copy(editUserGender = gender)
    }

    fun updateEditDateOfBirth(dateOfBirth: String) {
        _userData.value = _userData.value?.copy(editUserDateOfBirth = dateOfBirth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isValidDate(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateEditCurrentPassword(currentPassword: String) {
        _userData.value = _userData.value?.copy(editUserCurrentPassword = currentPassword)
    }

    fun updateEditNewPassword(newPassword: String) {
        _userData.value = _userData.value?.copy(editUserNewPassword = newPassword)
    }

    fun updateEditConfirmPassword(confirmPassword: String) {
        _userData.value = _userData.value?.copy(editUserConfirmPassword = confirmPassword)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveUserChanges() {
        viewModelScope.launch {
            val currentUserData = _userData.value
            val currentAuthUser = auth.currentUser
            val nowPassword = currentUserData?.password

            if (currentUserData == null || currentAuthUser == null) {
                _userData.value = currentUserData?.copy(
                    editUserErrorMessage = "No user logged in",
                    editUserIsLoading = false
                )
                return@launch
            }

            _userData.value = currentUserData.copy(
                editUserIsLoading = true,
                editUserErrorMessage = null
            )

            try {
                if (currentUserData.editUserName.isBlank()) {
                    _userData.value = currentUserData.copy(
                        editUserIsLoading = false,
                        editUserErrorMessage = "Name cannot be empty"
                    )
                    return@launch
                }

                if (currentUserData.editUserPhoneNum != currentUserData.phoneNum &&
                    !currentUserData.editUserPhoneNum.matches(Regex("^[0-9]{3}-[0-9]{7,8}$"))) {
                    _userData.value = currentUserData.copy(
                        editUserIsLoading = false,
                        editUserErrorMessage = "Phone Number must be in format: 000-0000000 or 000-00000000"
                    )
                    return@launch
                }

                if (currentUserData.editUserGender.isBlank()) {
                    _userData.value = currentUserData.copy(
                        editUserIsLoading = false,
                        editUserErrorMessage = "Please select a gender"
                    )
                    return@launch
                }

                if (currentUserData.editUserDateOfBirth.isNotBlank() && !isValidDate(currentUserData.editUserDateOfBirth)) {
                    _userData.value = currentUserData.copy(
                        editUserIsLoading = false,
                        editUserErrorMessage = "Date of birth must be in format (DD-MM-YYYY)"
                    )
                    return@launch
                }

                val isChangingPassword = currentUserData.editUserCurrentPassword.isNotBlank() ||
                        currentUserData.editUserNewPassword.isNotBlank() ||
                        currentUserData.editUserConfirmPassword.isNotBlank()

                if (isChangingPassword) {
                    if (currentUserData.editUserCurrentPassword != currentUserData.password) {
                        _userData.value = currentUserData.copy(
                            editUserIsLoading = false,
                            editUserErrorMessage = "Current password is not your original password"
                        )
                        return@launch
                    }

                    if (currentUserData.editUserNewPassword != currentUserData.editUserConfirmPassword) {
                        _userData.value = currentUserData.copy(
                            editUserIsLoading = false,
                            editUserErrorMessage = "New passwords do not match"
                        )
                        return@launch
                    }

                    if (currentUserData.editUserNewPassword.length < 8) {
                        _userData.value = currentUserData.copy(
                            editUserIsLoading = false,
                            editUserErrorMessage = "Password must be at least 8 characters"
                        )
                        return@launch
                    }

                    val credential = EmailAuthProvider.getCredential(
                        currentAuthUser.email!!,
                        currentUserData.editUserCurrentPassword
                    )
                    currentAuthUser.reauthenticate(credential).await()

                    currentAuthUser.updatePassword(currentUserData.editUserNewPassword).await()
                }

                if (currentUserData.editUserNewPassword.isBlank()) {
                    currentUserData.editUserNewPassword = nowPassword.toString()
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(currentUserData.editUserName)
                    .build()
                currentAuthUser.updateProfile(profileUpdates).await()

                val userUpdates = hashMapOf<String, Any>(
                    "name" to currentUserData.editUserName,
                    "phoneNum" to currentUserData.editUserPhoneNum,
                    "gender" to currentUserData.editUserGender,
                    "dateOfBirth" to currentUserData.editUserDateOfBirth,
                    "password" to currentUserData.editUserNewPassword
                )

                db.collection("Users").document(currentAuthUser.uid)
                    .update(userUpdates as Map<String, Any>)
                    .await()

                _userData.value = currentUserData.copy(
                    name = currentUserData.editUserName,
                    phoneNum = currentUserData.editUserPhoneNum,
                    gender = currentUserData.editUserGender,
                    dateOfBirth = currentUserData.editUserDateOfBirth,
                    password = currentUserData.editUserNewPassword,
                    editUserIsLoading = false,
                    editUserIsSuccess = true,
                    editUserErrorMessage = null,
                    editUserCurrentPassword = "",
                    editUserNewPassword = "",
                    editUserConfirmPassword = ""
                )

            } catch (e: Exception) {
                _userData.value = currentUserData.copy(
                    editUserIsLoading = false,
                    editUserErrorMessage = when {
                        e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                            "Current password is incorrect"
                        e.message?.contains("requires recent authentication") == true ->
                            "Please re-authenticate to change password"
                        else -> "Failed to update profile: ${e.message}"
                    }
                )
            }
        }
    }

    fun resetEditFields() {
        _userData.value = _userData.value?.let { currentUser ->
            currentUser.copy(
                editUserName = currentUser.name,
                editUserPhoneNum = currentUser.phoneNum,
                editUserGender = currentUser.gender,
                editUserDateOfBirth = currentUser.dateOfBirth,
                editUserCurrentPassword = "",
                editUserNewPassword = "",
                editUserConfirmPassword = "",
                editUserErrorMessage = null,
                editUserIsSuccess = false
            )
        }
    }

    fun clearEditError() {
        _userData.value = _userData.value?.copy(editUserErrorMessage = null)
    }

    fun resetEditSuccess() {
        _userData.value = _userData.value?.copy(editUserIsSuccess = false)
    }

    fun triggerStateChoiceNavigation() {
        viewModelScope.launch {
            _navigateToStateChoice.value = true
        }
    }

    fun resetNavigation() {
        viewModelScope.launch {
            _navigateToStateChoice.value = false
        }
    }

    fun clearUserData() {
        _userData.value = null
        _loadingState.value = LoadingState.Loading
    }

    sealed class LoadingState {
        object Loading : LoadingState()
        data class Success(val user: Account) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }
}

class UserSignInViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(Account())
    val uiState: StateFlow<Account> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun updateField(field: String, value: String) {
        val currentState = _uiState.value
        _uiState.value = when (field) {
            "email" -> currentState.copy(email = value, errorMessage = null)
            "password" -> currentState.copy(password = value, errorMessage = null)
            "confirmPassword" -> currentState.copy(confirmPassword = value, errorMessage = null)
            "name" -> currentState.copy(name = value, errorMessage = null)
            "gender" -> currentState.copy(gender = value, errorMessage = null)
            "areaCode" -> currentState.copy(areaCode = value, errorMessage = null)
            "phoneNumber" -> currentState.copy(phoneNumber = value, errorMessage = null)
            "dateOfBirth" -> currentState.copy(dateOfBirth = value, errorMessage = null)
            else -> currentState
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@gmail\\.com$")
        return emailPattern.matcher(email).matches()
    }

    private fun isValidPhone(areaCode: String, phoneNumber: String): Boolean {
        return areaCode.length == 3 && phoneNumber.length in 7..8
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isValidDate(dateStr: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            LocalDate.parse(dateStr, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateForm(): String? {
        val state = _uiState.value
        return when {
            state.name.isBlank() -> "Please enter your full name"
            state.email.isBlank() -> "Please enter your email"
            !isValidEmail(state.email) -> "Email must be in format: ...@gmail.com"
            state.areaCode.isBlank() || state.phoneNumber.isBlank() -> "Please enter your phone number"
            !isValidPhone(state.areaCode, state.phoneNumber) -> "Phone must be in format: 000-0000000"
            state.gender.isBlank() -> "Please select your gender"
            state.dateOfBirth.isBlank() -> "Please select your date of birth"
            !isValidDate(state.dateOfBirth) -> "Date of birth must be valid"
            state.password.isBlank() -> "Please enter a password"
            state.password.length < 8 -> "Password must be at least 8 characters"
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.password != state.confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    private suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val userQuery = db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .await()
            !userQuery.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun generateUserId(): String {
        return try {
            val querySnapshot = db.collection("Users").get().await()
            val currentCount = querySnapshot.size()
            val formattedId = "U${currentCount.toString().padStart(4, '0')}"

            Log.d("UserIdGenerator", "Current users: $currentCount, New ID: $formattedId")
            formattedId
        } catch (e: Exception) {
            Log.e("UserIdGenerator", "Error counting users: ${e.message}")
            "U${System.currentTimeMillis().toString().takeLast(4).padStart(4, '0')}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun signUp(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validationError = validateForm()
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            onError(validationError)
            return
        }

        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val emailExists = checkEmailExists(state.email)
                if (emailExists) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = "Email already exists"
                    )
                    onError("Email already exists")
                    return@launch
                }

                val authResult = auth.createUserWithEmailAndPassword(
                    state.email,
                    state.password
                ).await()

                val userId = authResult.user?.uid ?: throw Exception("User ID is null")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(state.name)
                    .build()
                authResult.user?.updateProfile(profileUpdates)?.await()

                val customUserId = generateUserId()

                val user = hashMapOf(
                    "userId" to customUserId,
                    "name" to state.name,
                    "email" to state.email,
                    "phoneNum" to "${state.areaCode}-${state.phoneNumber}",
                    "gender" to state.gender,
                    "dateOfBirth" to state.dateOfBirth,
                    "password" to state.password
                )

                db.collection("Users").document().set(user).await()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Registration failed: ${e.message}"
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                onError(errorMsg)
            }
        }
    }
}

class UserLoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(Account())
    val uiState: StateFlow<Account> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun login(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentState = _uiState.value

//        _uiState.value = currentState.copy(
//            isLoading = false,
//            userId = "dummyUserId",
//            name = "Test User",
//            errorMessage = null
//        )
//
//
//        onSuccess()


        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Please fill in all fields")
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(
                    currentState.email,
                    currentState.password
                ).await()

                authResult?.user?.let { firebaseUser ->
                    val firebaseUid = firebaseUser.uid
                    val userDoc = db.collection("Users").document(firebaseUid).get().await()
                    val userId = userDoc.getString("userId") ?: ""

                    try {
                        if (!userDoc.exists()) throw Exception("User record not found in Firestore")



                        val userName = userDoc.getString("name") ?: ""
                        val phoneNum = userDoc.getString("phoneNum") ?: ""

                        // Save current user session
                        UserSession.currentAccount = Account(
                            userId = userId,
                            name = userName,
                            email = currentState.email,
                            phoneNum = phoneNum
                        )


                        _uiState.value = currentState.copy(
                            isLoading = false,
                            userId = userId,
                            name = userName,
                            errorMessage = null
                        )
                        onSuccess()
                    } catch (firestoreEx: Exception) {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            userId = userId,
                            errorMessage = null
                        )
                        onSuccess()

                    }
                } ?: run {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Login failed: No user returned"
                    )
                    onError("Login failed: No user returned")
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("invalid email") == true -> "Invalid email format"
                    e.message?.contains("user-not-found") == true -> "No account found with this email"
                    e.message?.contains("wrong-password") == true -> "Incorrect password"
                    else -> "Login failed: ${e.message}"
                }
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                onError(errorMsg)
            }
        }
    }
}

class ResetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(Account())
    val uiState: StateFlow<Account> = _uiState.asStateFlow()
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email cannot be empty") }
            onError("Email cannot be empty")
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Invalid email format") }
            onError("Invalid email format")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val querySnapshot = db.collection("Users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No account found with this email",
                            successMessage = null
                        )
                    }
                    onError("No account found with this email")
                } else {
                    auth.sendPasswordResetEmail(email).await()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Email verified. You can now reset your password.",
                            errorMessage = null
                        )
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to check email: ${e.message}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg,
                        successMessage = null
                    )
                }
                onError(errorMsg)
            }
        }
    }

    fun resetPasswordDirectly(
        email: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        when {
            email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Email is required") }
                onError("Email is required")
                return
            }
            newPassword.isBlank() || confirmPassword.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Password fields cannot be empty") }
                onError("Password fields cannot be empty")
                return
            }
            newPassword.length < 8 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
                onError("Password must be at least 8 characters")
                return
            }
            newPassword != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
                onError("Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val querySnapshot = db.collection("Users").whereEqualTo("email", email).limit(1).get().await()

                if (querySnapshot.isEmpty) {
                    throw Exception("Account not found")
                }

                val userDoc = querySnapshot.documents[0]
                val userId = userDoc.id
                val currentPassword = userDoc.getString("password") ?: ""
                var firebaseUser: com.google.firebase.auth.FirebaseUser? = null

                try {
                    // Try to get user by email from Firebase Auth
                    val authUsers = auth.fetchSignInMethodsForEmail(email).await()
                    if (authUsers.signInMethods?.isNotEmpty() == true) {
                        if (currentPassword.isNotBlank()) {
                            try {
                                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                                val authResult = auth.signInWithCredential(credential).await()
                                firebaseUser = authResult.user
                            } catch (e: Exception) {
                                Log.w("ResetPassword", "Could not sign in: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("ResetPassword", "Could not fetch auth user: ${e.message}")
                }

                if (firebaseUser != null) {
                    try {
                        firebaseUser.updatePassword(newPassword).await()
                        Log.d("ResetPassword", "Firebase Auth password updated successfully")
                    } catch (e: Exception) {
                        if (e.message?.contains("requires recent authentication") == true && currentPassword.isNotBlank()) {
                            try {
                                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                                firebaseUser.reauthenticate(credential).await()
                                firebaseUser.updatePassword(newPassword).await()
                                Log.d("ResetPassword", "Firebase Auth password updated after reauth")
                            } catch (reauthError: Exception) {
                                Log.e("ResetPassword", "Reauth failed: ${reauthError.message}")
                            }
                        } else {
                            Log.e("ResetPassword", "Failed to update Firebase Auth: ${e.message}")
                        }
                    }
                }

                db.collection("Users").document(userId)
                    .update("password", newPassword)
                    .await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Password reset successfully!",
                        errorMessage = null,
                        password = "",
                        confirmPassword = ""
                    )
                }
                onSuccess()

            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("not-found") == true -> "Account not found"
                    else -> "Failed to reset password: ${e.message}"
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg,
                        successMessage = null
                    )
                }
                onError(errorMsg)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}


class AboutViewModel : ViewModel() {
    private val _navigateToStateChoice = MutableStateFlow(false)
    val navigateToStateChoice: StateFlow<Boolean> = _navigateToStateChoice.asStateFlow()

    private val _uiState = MutableStateFlow<Abouts?>(null)
    val uiState: StateFlow<Abouts?> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    init { loadAbout() }

    fun loadAbout() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    _loadingState.value = LoadingState.Loading
                    val userDoc = db.collection("Abouts").document("Abouts").get().await()

                    if (userDoc.exists()) {
                        val about = Abouts(
                            title = userDoc.getString("title") ?: "",
                            content = userDoc.getString("content") ?: "",
                            edittitle = userDoc.getString("title") ?: "",
                            editcontent = userDoc.getString("content") ?: "",
                        )
                        _uiState.value = about
                        _loadingState.value = LoadingState.Success(about)
                    } else {
                        val defaultAbout = Abouts(
                            title = "Yum Yum Restaurant",
                            content = "Non",
                            edittitle = "Yum Yum Restaurant",
                            editcontent = "Add your about information here"
                        )
                        _uiState.value = defaultAbout
                        _loadingState.value = LoadingState.Success(defaultAbout)
                    }
                } catch (e: Exception) {
                    _loadingState.value = LoadingState.Error("Failed to load user data: ${e.message}")
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value?.copy(edittitle = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value?.copy(editcontent = content)
    }

    fun saveAboutChanges() {
        viewModelScope.launch {
            try {
                val currentAbout = _uiState.value
                val currentUser = auth.currentUser

                if (currentAbout == null || currentUser == null) {
                    _loadingState.value = LoadingState.Error("Cannot save: No data or user not authenticated")
                    return@launch
                }

                _loadingState.value = LoadingState.Loading

                val userUpdates = hashMapOf<String, Any>(
                    "title" to currentAbout.edittitle,
                    "content" to currentAbout.editcontent
                )

                db.collection("Abouts")
                    .document("Abouts")
                    .update(userUpdates)
                    .await()

                val updatedAbout = currentAbout.copy(
                    title = currentAbout.edittitle,
                    content = currentAbout.editcontent,
                    edittitle = currentAbout.edittitle,
                    editcontent = currentAbout.editcontent
                )

                _uiState.value = updatedAbout
                _loadingState.value = LoadingState.Success(updatedAbout)

            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Failed to save changes: ${e.message}")
            }
        }
    }

    sealed class LoadingState {
        object Loading : LoadingState()
        data class Success(val about: Abouts) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class ReportViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _reservations = MutableStateFlow<List<ReservationEntity>>(emptyList())
    val reservations: StateFlow<List<ReservationEntity>> = _reservations.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int>(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _reportType = MutableStateFlow<String>("")
    val reportType: StateFlow<String> = _reportType.asStateFlow()

    private val _sortedMonthlyReservations = MutableStateFlow<List<MonthlyReservationData>>(emptyList())
    val sortedMonthlyReservations: StateFlow<List<MonthlyReservationData>> = _sortedMonthlyReservations.asStateFlow()

    private val _sortedMonthlyPayments = MutableStateFlow<List<MonthlyPaymentData>>(emptyList())
    val sortedMonthlyPayments: StateFlow<List<MonthlyPaymentData>> = _sortedMonthlyPayments.asStateFlow()

    private val _monthlyReservations = MutableStateFlow<List<MonthlyReservationData>>(emptyList())
    val monthlyReservations: StateFlow<List<MonthlyReservationData>> = _monthlyReservations.asStateFlow()

    private val _monthlyPayments = MutableStateFlow<List<MonthlyPaymentData>>(emptyList())
    val monthlyPayments: StateFlow<List<MonthlyPaymentData>> = _monthlyPayments.asStateFlow()

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun setReportType(type: String) {
        _reportType.value = type
        clearData()
    }

    fun fetchDataByYear() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val year = _selectedYear.value
                val type = _reportType.value

                val currentYear = LocalDate.now().year
                if (year !in 0..currentYear) {
                    throw IllegalArgumentException("Year must be between 0 and $currentYear")
                }

                if (type.isEmpty()) {
                    throw IllegalArgumentException("Please select a report type")
                }

                clearData()

                when (type) {
                    "Reservation Report" -> {
                        getAllReservationsForYear(year)
                        processMonthlyReservations()
                    }
                    "Sales Report" -> {
                        getAllPaymentsForYear(year)
                        processMonthlyPayments()
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error fetching data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getAllReservationsForYear(year: Int) {
        try {
            val startDate = "$year-01-01"
            val endDate = "${year + 1}-01-01"

            val querySnapshot = db.collection("Reservations")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThan("date", endDate)
                .get()
                .await()

            val reservationsList = querySnapshot.documents.mapNotNull { document ->
                document.toObject(ReservationEntity::class.java)?.apply {
                    reservationId = document.id
                }
            }

            _reservations.value = reservationsList

        } catch (e: Exception) {
            throw Exception("Failed to fetch reservations: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getAllPaymentsForYear(year: Int) {
        try {
            val calendar = Calendar.getInstance().apply {
                set(year, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfYear = Timestamp(calendar.time)

            calendar.add(Calendar.YEAR, 1)
            val startOfNextYear = Timestamp(calendar.time)

            val querySnapshot = db.collection("Payments")
                .whereGreaterThanOrEqualTo("PaymentDate", startOfYear)
                .whereLessThan("PaymentDate", startOfNextYear)
                .get()
                .await()

            val paymentsList = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Payment::class.java)?.apply {
                    paymentID = document.id
                }?.takeIf { it.paymentDate != null }
            }

            _payments.value = paymentsList
        } catch (e: Exception) {
            throw Exception("Failed to fetch payments: ${e.message}")
        }
    }

    private fun processMonthlyReservations() {
        val reservationsList = _reservations.value
        val monthlyData = (1..12).map { month ->
            MonthlyReservationData(
                month = month,
                monthName = getMonthName(month),
                reservationCount = 0,
                guestCount = 0,
                reservations = emptyList()
            )
        }.toMutableList()

        val groupedByMonth = reservationsList.groupBy { reservation ->
            try {
                val dateParts = reservation.date.split("-")
                if (dateParts.size >= 2) dateParts[1].toInt() else 0
            } catch (e: Exception) { 0 }
        }

        groupedByMonth.forEach { (month, reservations) ->
            if (month in 1..12) {
                val monthIndex = month - 1
                monthlyData[monthIndex] = MonthlyReservationData(
                    month = month,
                    monthName = getMonthName(month),
                    reservationCount = reservations.size,
                    guestCount = reservations.sumOf { it.guestCount },
                    reservations = reservations
                )
            }
        }

        _monthlyReservations.value = monthlyData
        val sortedData = monthlyData.sortedByDescending { it.reservationCount }
        _sortedMonthlyReservations.value = sortedData
    }

    private fun processMonthlyPayments() {
        val paymentsList = _payments.value
        val monthlyData = (1..12).map { month ->
            MonthlyPaymentData(
                month = month,
                monthName = getMonthName(month),
                totalAmount = 0.0,
                transactionCount = 0,
                payments = emptyList()
            )
        }.toMutableList()

        val groupedByMonth = paymentsList.groupBy { payment ->
            payment.paymentDate?.toDate()?.let { date ->
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH) + 1
            } ?: 0
        }

        groupedByMonth.forEach { (month, payments) ->
            if (month in 1..12) {
                val monthIndex = month - 1
                monthlyData[monthIndex] = MonthlyPaymentData(
                    month = month,
                    monthName = getMonthName(month),
                    totalAmount = payments.sumOf { it.amountPaid },
                    transactionCount = payments.size,
                    payments = payments
                )
            }
        }

        _monthlyPayments.value = monthlyData
        val sortedData = monthlyData.sortedByDescending { it.totalAmount }
        _sortedMonthlyPayments.value = sortedData
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }

    fun clearData() {
        _reservations.value = emptyList()
        _payments.value = emptyList()
        _monthlyReservations.value = emptyList()
        _monthlyPayments.value = emptyList()
        _sortedMonthlyReservations.value = emptyList()
        _sortedMonthlyPayments.value = emptyList()
    }
}

data class MonthlyReservationData(
    val month: Int,
    val monthName: String,
    val reservationCount: Int,
    val guestCount: Int,
    val reservations: List<ReservationEntity>
)

data class MonthlyPaymentData(
    val month: Int,
    val monthName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val payments: List<Payment>
)

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "Reservations")
@TypeConverters(Converters::class)
data class ReservationEntity(
    @PrimaryKey
    var reservationId: String = "",
    var date: String = "",
    var startTime: String = "",
    var durationMinutes: Int = 15,
    var endTime: String = "",

    var guestCount: Int = 2,
    var zone: String = "INDOOR",
    var specialRequests: String = "",
    var reservationStatus: String = "Confirmed",
    var userId: String = ""
)

data class Payment(
    @get:PropertyName("PaymentID") @set:PropertyName("PaymentID") var paymentID: String = "",
    @get:PropertyName("PaymentMethod") @set:PropertyName("PaymentMethod") var paymentMethod: String = "",
    @get:PropertyName("PaymentDate") @set:PropertyName("PaymentDate") var paymentDate: Timestamp? = null,
    @get:PropertyName("AmountPaid") @set:PropertyName("AmountPaid") var amountPaid: Double = 0.0,
)