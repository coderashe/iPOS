package com.example.learncompose303.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.learncompose303.R
import com.example.learncompose303.app.AutoSlidingCarousel
import com.example.learncompose303.app.CustomHeadingMedium
import com.example.learncompose303.app.SalesmanInfoCard
import com.example.learncompose303.app.SpacerHeight
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.screens.auth.AuthState
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.viewmodels.InvoiceViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: InvoiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {

    val authState = authViewModel.authState.observeAsState()
    val dayStarted by viewModel.dayStarted.collectAsState() // Observe the dayStarted state from the ViewModel

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate(Destinations.SignIn.route)
            else -> Unit
        }
    }

    val currentUserEmail by authViewModel.userEmail.observeAsState()

    // Trigger fetch when the email is available
    currentUserEmail?.let {
        LaunchedEffect(it) {
            authViewModel.fetchUserByEmail(it)
        }
        Log.d("StartLogs", "current loggedin user is $currentUserEmail")
    }

    val currentUser by authViewModel.fetchedUser.observeAsState()
    // Logging the fetched user once available
    currentUser?.let {
        Log.d("StartLogs", "Current logged-in user is ${it.userFullName}")
    } ?: run {
        Log.d("StartLogs", "No user found with this email.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(top = 4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SalesmanInfoCard(
            salesmanName = currentUser?.userFullName ?: "iSTART",
            shopName = currentUser?.storeID ?: "ISTART CAPITAL STORE",
            viewModel = viewModel
        )
        SpacerHeight()
        QuickAccessButtons(navController)
        SpacerHeight()
        CustomHeadingMedium(
            heading = stringResource(R.string.popular_products)
        )
        AutoSlidingCarousel(modifier = Modifier, navController, viewModel)
        SpacerHeight()
    }
}

