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
import androidx.work.BackoffPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.learncompose303.app.core.DeviceSession
import com.example.learncompose303.data.repositories.BackfillRepository
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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.learncompose303.network.NetworkResult
import com.example.learncompose303.network.SyncUserWorker
import com.example.learncompose303.network.UserApiService
import com.example.learncompose303.network.UserDto
import com.example.learncompose303.workmanager.LogoutWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userApiService: UserApiService,
    private val workManager: WorkManager,
    private val backfillRepository: BackfillRepository,
    private val deviceSession: DeviceSession
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private val _fetchedUser = MutableLiveData<User?>()
    val fetchedUser: LiveData<User?> = _fetchedUser

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    private var isManuallyLoggedOut = false

    private val _users = MutableStateFlow<NetworkResult<List<UserDto>>>(NetworkResult.Loading())
    val users: StateFlow<NetworkResult<List<UserDto>>> = _users

    init {
        checkAuthStatus()
        scheduleMonthlyLogout()
    }

    // Function to load all users
    fun loadAllUsers() {
        viewModelScope.launch {
            _users.value = NetworkResult.Loading()
            val result = authRepository.fetchAllUsers()
            _users.value = result

            // Log the fetched users
            when (result) {
                is NetworkResult.Success -> {
                    val usersList = result.data

                    // Log each user individually
                    usersList.forEachIndexed { index, user ->
                    }
                }

                is NetworkResult.Error -> {
                    Log.e("UsersData", "Error fetching users: ${result.message}")
                }

                is NetworkResult.Loading -> {
                }
            }
        }
    }

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }

    // Method to fetch the user's role by email
    fun fetchUserRoleByEmail(email: String) {
        viewModelScope.launch {
            val user = authRepository.getUserByEmail(email)
            _userRole.value = user?.userRole
        }
    }

    // Check user authentication status
    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.UnAuthenticated
            _userEmail.value = null
        } else {
            _authState.value = AuthState.Authenticated
            _userEmail.value = currentUser.email
            if (!isManuallyLoggedOut) {
                scheduleMonthlyLogout()
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
                    _userEmail.value = auth.currentUser?.email
                    auth.currentUser?.email?.let { fetchUserRoleByEmail(it) }
                    resetLogoutState()
                    // Backfill deviceId/storeId on any legacy records missing them
                    viewModelScope.launch {
                        if (deviceSession.deviceId.isNotBlank() && deviceSession.storeId.isNotBlank()) {
                            backfillRepository.backfillDeviceAndStore(
                                deviceId = deviceSession.deviceId,
                                storeId = deviceSession.storeId
                            )
                        }
                    }
                } else {
                    val error = task.exception?.localizedMessage ?: "Unknown error"
                    Log.d("AuthViewModel", error)
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

        viewModelScope.launch {
            val existingUser = authRepository.getUserByEmail(email)
            if (existingUser != null) {
                _authState.value = AuthState.Error("Email is already registered.")
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = task.result?.user
                            viewModelScope.launch {
                                val finalCheckUser = authRepository.getUserByEmail(email)
                                if (finalCheckUser != null) {
                                    _authState.value =
                                        AuthState.Error("This email is already registered in the local database.")
                                } else {
                                    val newUser = User(
                                        userFullName = fullName,
                                        userContact = contact,
                                        userRole = role,
                                        userPassword = password,
                                        storeID = storeID,
                                        userEmail = email,
                                        userAddress = address
                                    )
//                                    Save to localDB
                                    authRepository.signUpUser(newUser)
                                    _authState.value = AuthState.SignUpSuccess

//                                    connect to server and save to remote db
//                                    try {
//                                        // Convert local User entity to UserDto for API
//                                        val userDto = UserDto(
//                                            name = fullName,
//                                            contact = contact,
//                                            role = role,
//                                            password = password,
//                                            email = email,
//                                            store = storeID,
//                                            address = address
//                                        )
//                                        val result = authRepository.saveUserRemote(userDto)
//                                        when (result) {
//                                            is NetworkResult.Success -> {
//                                                _authState.value = AuthState.SignUpSuccess
//                                            }
//
//                                            is NetworkResult.Error -> {
//                                                _authState.value = AuthState.Error(result.message)
//                                            }
//
//                                            else -> {
//                                                Log.e(
//                                                    "UsersData",
//                                                    "Failed to save user remotely"
//                                                )
//                                            }
//                                        }
//                                    } catch (e: Exception) {
//                                        // Handle network exceptions
//                                        Log.e(
//                                            "UsersData",
//                                            "Network error saving user remotely",
//                                            e
//                                        )
//                                        // Optionally handle retry logic or queue work for later sync
//                                    }
//                                    end of remote save
                                }
                            }
                        } else {
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                _authState.value = AuthState.Error("This email is already in use.")
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

    // Fetch a user by email
    fun fetchUserByEmail(email: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getUserByEmail(email)
                _fetchedUser.value = user
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user: ${e.message}")
                _authState.value = AuthState.Error("Failed to fetch user.")
            }
        }
    }

    // Manual Signout method
    fun signout() {
        isManuallyLoggedOut = true
        performLogout()
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
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Failed to send reset email")
                }
            }
    }

    // Perform Logout
    private fun performLogout() {
        auth.signOut()
        _authState.value = AuthState.UnAuthenticated
    }

    // Resets the logout state after successful login/signup
    private fun resetLogoutState() {
        isManuallyLoggedOut = false
        scheduleMonthlyLogout()
    }

    // Method to be called when user interacts with the app to reset the timer
    fun onUserInteraction() {
        // Do nothing here to allow timer to continue running
    }

    private fun scheduleMonthlyLogout() {
        val currentTime = Calendar.getInstance()

        // Set the target logout time to today at 09:50 AM
        val testLogoutTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)  // set hours
            set(Calendar.MINUTE, 0)      //  set minutes
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val delay = testLogoutTime.timeInMillis - currentTime.timeInMillis

        if (delay > 0) {
            val logoutRequest = OneTimeWorkRequestBuilder<LogoutWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniqueWork(
                "monthly_logout",
                ExistingWorkPolicy.REPLACE,
                logoutRequest
            )
        } else {
        }
    }

    fun scheduleUserSync(user: User) {
        val syncUserWork = OneTimeWorkRequestBuilder<SyncUserWorker>()
            .setInputData(
                workDataOf(
                    "userId" to user.userId,
                    "retry" to true
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15, TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_user_${user.userId}",
            ExistingWorkPolicy.REPLACE,
            syncUserWork
        )
    }

}

