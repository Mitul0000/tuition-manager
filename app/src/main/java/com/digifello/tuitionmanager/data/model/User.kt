package com.digifello.tuitionmanager.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false
)