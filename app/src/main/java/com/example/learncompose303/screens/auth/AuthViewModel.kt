package com.example.learncompose303.screens.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.learncompose303.data.entities.User
import com.example.learncompose303.data.repositories.AuthRepository
import com.example.learncompose303.workmanager.MonthlyLogoutWorker
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.work.ExistingPeriodicWorkPolicy

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState // Exposing it to UI

    // LiveData for current user's email
    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    // LiveData for fetched user
    private val _fetchedUser = MutableLiveData<User?>()
    val fetchedUser: LiveData<User?> = _fetchedUser

    // LiveData for user role
    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    // Method to fetch the user's role by email
    fun fetchUserRoleByEmail(email: String) {
        viewModelScope.launch {
            val user = authRepository.getUserByEmail(email)
            _userRole.value = user?.userRole
        }
    }

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
        // Fetch the role when the user logs in
        _userEmail.value?.let { fetchUserRoleByEmail(it) }
    }

    // Check user status
    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.UnAuthenticated
            _userEmail.value = null
        } else {
            _authState.value = AuthState.Authenticated
            _userEmail.value = currentUser.email // Store the user's email
            if (!isManuallyLoggedOut) {
                startLogoutTimer()
            }
        }
    }

    // Method to log in
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
                    _userEmail.value = auth.currentUser?.email // Update email on successful login

                    // Fetch the user role immediately after login
                    auth.currentUser?.email?.let { email ->
                        fetchUserRoleByEmail(email)
                    }

                    resetLogoutState()
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    // Signup method
    fun signup(
        fullName: String,
        contact: String,
        email: String,
        password: String,
        role: String,
        storeID: String,
        address: String?
    ) {
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || contact.isEmpty()) {
            _authState.value = AuthState.Error("All fields must be filled")
            return
        }

        _authState.value = AuthState.Loading

        // Check if the email already exists in the local database
        viewModelScope.launch {
            val existingUser =
                authRepository.getUserByEmail(email) // Query to check if email exists

            if (existingUser != null) {
                _authState.value =
                    AuthState.Error("Email is already registered. Please use a different email.")
            } else {
                // If the email does not exist in the local DB, proceed with Firebase signup
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = task.result?.user

                            // Perform a final check before saving the user to the local DB
                            viewModelScope.launch {
                                val finalCheckUser = authRepository.getUserByEmail(email)
                                if (finalCheckUser != null) {
                                    _authState.value =
                                        AuthState.Error("This email is already registered in the local database.")
                                } else {
                                    // Proceed with saving the user to local DB
                                    val newUser = User(
                                        userFullName = fullName,
                                        userContact = contact,
                                        userRole = role,
                                        userPassword = password, // Consider hashing the password
                                        storeID = storeID,
                                        userEmail = email,
                                        userAddress = address
                                    )

                                    authRepository.signUpUser(newUser) // Save user to local DB
                                    _authState.value =
                                        AuthState.SignUpSuccess // Set the state to SignUpSuccess instead of Authenticated
                                }
                            }
                        } else {
                            // Check if Firebase returned an error due to email duplication
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                _authState.value =
                                    AuthState.Error("This email is already in use. Please try another email.")
                            } else {
                                _authState.value = AuthState.Error(
                                    task.exception?.message ?: "Something went wrong"
                                )
                            }
                        }
                    }
            }
        }
    }


    // Function to fetch a user by email
    fun fetchUserByEmail(email: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getUserByEmail(email)
                _fetchedUser.value = user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user by email: ${e.message}")
                _authState.value = AuthState.Error("Failed to fetch user.")
            }
        }
    }

    // Manual Signout method
    fun signout() {
        Log.d("AuthViewModel", "Manual signout called. Signing out.")
        isManuallyLoggedOut = true // Set flag to true on manual logout
        performLogout() // Perform logout directly
    }

    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email to reset the password")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success("Password reset email sent successfully")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Failed to send reset email")
                }
            }
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

    private fun startLogoutTimer() {
        // Calculate the time remaining until the next Monday
        val currentTime = Calendar.getInstance()

        // Set the day of the week for the next Monday
        val nextLogoutDay = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)

            // If today is already Monday, and the current time has passed, set to next Monday
            if (currentTime.timeInMillis >= timeInMillis) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
            // Reset the time to 00:00:00 for logout at midnight
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Calculate the time difference between now and the next Monday
        val timeUntilNextMonday = nextLogoutDay.timeInMillis - currentTime.timeInMillis

        Log.d("AuthViewModel", "Starting logout timer for next LogoutDay.")

        // Schedule the logout for the calculated time
        logoutHandler.postDelayed(logoutRunnable, timeUntilNextMonday)
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
