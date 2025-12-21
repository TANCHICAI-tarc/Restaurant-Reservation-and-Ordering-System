package com.example.yumyumrestaurant


data class Account(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val password:String = "",
    val phoneNum: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",

    //for sign in
    val confirmPassword: String = "",
    val areaCode: String = "",
    val phoneNumber: String = "",

    //check error
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Edit state properties
    val editUserName: String = "",
    val editUserPhoneNum: String = "",
    val editUserGender: String = "",
    val editUserDateOfBirth: String = "",
    val editUserCurrentPassword: String = "",
    var editUserNewPassword: String = "",
    val editUserConfirmPassword: String = "",
    val editUserIsLoading: Boolean = false,
    val editUserErrorMessage: String? = null,
    val editUserIsSuccess: Boolean = false
)

data class Abouts(
    val title: String = "",
    val content: String = "",
    val edittitle: String = "",
    val editcontent: String = ""
)