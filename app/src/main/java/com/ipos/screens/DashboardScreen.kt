package com.ipos.screens

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
import com.ipos.R
import com.ipos.app.AutoSlidingCarousel
import com.ipos.app.CustomHeadingMedium
import com.ipos.app.SalesmanInfoCard
import com.ipos.app.SalesmanMonthlyInfoCard
import com.ipos.app.SalesmanWeeklyInfoCard
import com.ipos.app.SpacerHeight
import com.ipos.app.SyncStatusCard
import com.ipos.data.Destinations
import com.ipos.screens.auth.AuthState
import com.ipos.screens.auth.AuthViewModel
import com.ipos.viewmodels.InvoiceViewModel

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
    }

    val currentUser by authViewModel.fetchedUser.observeAsState()
    // Logging the fetched user once available
    currentUser?.let {
    } ?: run {
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(top = 4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SalesmanInfoCard(
//        SalesmanWeeklyInfoCard(
//        SalesmanMonthlyInfoCard(
            salesmanName = currentUser?.userFullName ?: "iSTART",
            shopName = currentUser?.storeID ?: "ISTART CAPITAL STORE",
            viewModel = viewModel
        )
        SpacerHeight()
        QuickAccessButtons(navController,viewModel)
        SpacerHeight()
        CustomHeadingMedium(
            heading = stringResource(R.string.popular_products)
        )
        AutoSlidingCarousel(modifier = Modifier, navController, viewModel)
        SpacerHeight()
        SyncStatusCard(viewModel = viewModel)
        SpacerHeight()
    }
}


