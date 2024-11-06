package com.example.learncompose303.screens.auth

sealed class AuthState {
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Loading : AuthState()
    object SignUpSuccess : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()

}

