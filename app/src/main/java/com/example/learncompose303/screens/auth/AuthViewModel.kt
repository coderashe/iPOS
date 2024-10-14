package com.example.learncompose303.screens.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.learncompose303.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class AuthViewModel(
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState // Exposing it to UI

    // Handler and Runnable for logout after inactivity
    private val logoutHandler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable {
        Log.d("AuthViewModel", "Logout timer expired. Proceeding with logout...")
        performLogout() // Call the logout method when timer expires
    }

    // Flag to track if user logged out manually
    private var isManuallyLoggedOut = false

    init {
        checkAuthStatus() // Check auth status on ViewModel initialization
    }

    // Check user status
    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.UnAuthenticated
        } else {
            _authState.value = AuthState.Authenticated
            if (!isManuallyLoggedOut) {
                startLogoutTimer() // Start timer only if not manually logged out
            }
        }
    }

    // Login method
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    resetLogoutState() // Reset manual logout flag and start timer
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    // Signup method
    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    resetLogoutState() // Reset manual logout flag and start timer
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    // Manual Signout method
    fun signout() {
        Log.d("AuthViewModel", "Manual signout called. Signing out.")
        isManuallyLoggedOut = true // Set flag to true on manual logout
        performLogout() // Perform logout directly
    }

    // Function that handles the actual logout process
    private fun performLogout() {
        Log.d("AuthViewModel", "Performing logout.")
        auth.signOut() // Perform the actual signout
        _authState.value = AuthState.UnAuthenticated
        Log.d("AuthViewModel", "User logged out successfully.")
    }

    // Resets the logout state after successful login or signup
    private fun resetLogoutState() {
        isManuallyLoggedOut = false // Reset manual logout flag
        startLogoutTimer() // Start the timer upon successful login/signup
    }

    // Start the logout timer (1 minute or 60,000 milliseconds)
    private fun startLogoutTimer() {
        val oneMinuteInMillis = 60 * 1000L // 1 minute in milliseconds
        val fiveMinutesInMillis = 5 * 60 * 1000L // 5 minutes in milliseconds
        val oneDayInMillis = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        val oneWeekInMillis = 168 * 60 * 60 * 1000L // 168 hours in milliseconds
        Log.d("AuthViewModel", "Starting logout timer for 1 minute.")
        logoutHandler.postDelayed(logoutRunnable, oneDayInMillis) // 1 minute delay
    }

    // Stop the logout timer
    private fun stopLogoutTimer() {
        Log.d("AuthViewModel", "Stopping logout timer.")
        logoutHandler.removeCallbacks(logoutRunnable)
    }

    // Method to be called when user interacts with the app to reset the timer
    fun onUserInteraction() {
        Log.d("AuthViewModel", "User interaction detected. Timer will continue running.")
        // Do nothing here to allow timer to continue running
    }

    // Clean up the handler when the ViewModel is cleared
    override fun onCleared() {
        Log.d("AuthViewModel", "ViewModel cleared. Stopping logout timer.")
        super.onCleared()
        stopLogoutTimer() // Stop the timer when ViewModel is destroyed
    }
}
