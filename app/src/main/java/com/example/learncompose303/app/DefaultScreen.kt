package com.example.learncompose303.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.*
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.ui.theme.MyGray
import kotlinx.coroutines.launch
import com.example.learncompose303.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.learncompose303.screens.CartScreen
import com.example.learncompose303.screens.CustomerSelectionScreen
import com.example.learncompose303.screens.InvoiceDetailScreen
import com.example.learncompose303.screens.InvoicingScreen
import com.example.learncompose303.screens.LoadStockScreen
import com.example.learncompose303.screens.LoginScreen
import com.example.learncompose303.screens.MyIncomeScreen
import com.example.learncompose303.screens.OrderScreen
import com.example.learncompose303.screens.OrdersReportsScreen
import com.example.learncompose303.screens.PriorityScreen
import com.example.learncompose303.screens.ProductSelectionScreen
import com.example.learncompose303.screens.ReportsScreen
import com.example.learncompose303.screens.RideRequestsScreen
import com.example.learncompose303.screens.WalletScreen
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.screens.SignInPage
import com.example.learncompose303.screens.SignUpPage
import com.example.learncompose303.viewmodels.InvoiceViewModel


@Composable
fun DefaultScreen() {
    Surface {
// TODO:    Test ui here
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InDriveTopBottomNavigation() {
    val viewModel: InvoiceViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var isChecked by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isChecked) "Online" else "Offline",
                            modifier = Modifier.padding(end = 8.dp),
                            color = if (isChecked) Color.Green else Color.Red
                        )
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it }
                        )
                    }
                },
                backgroundColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                }
            )
        },
        drawerContent = {
            IndriveDrawerNav(navController = navController, scaffoldState)
        },
        bottomBar = {
            IndriveBottomNavigation(navController)
        },
    ) { paddingValues ->
//        set your starting destination of the app
        NavHost(
            navController = navController,
            startDestination = Destinations.SignIn.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Destinations.SignUp.route) { SignUpPage(navController, authViewModel) }
            composable(Destinations.SignIn.route) { SignInPage(navController, authViewModel) }
            composable(Destinations.Rides.route) { RideRequestsScreen(navController, viewModel) }
            composable(Destinations.Income.route) { MyIncomeScreen() }
            composable(Destinations.Priority.route) { PriorityScreen() }
            composable(Destinations.Wallet.route) { WalletScreen() }
            composable(Destinations.Invoice.route) { InvoicingScreen() }

            // Customer Selection Screen
            composable(Destinations.CustomerSelection.route) {
                CustomerSelectionScreen(
                    onCustomerSelected = { customer ->
                        navController.navigate(Destinations.ProductSelection.route + "/${customer.customerId}")
                    },
                    onNewCustomerClicked = {
                        // Handle new customer logic
                    },
                    navController = navController,
                    viewModel = viewModel
                )
            }

// Product Selection Screen
            composable(Destinations.ProductSelection.route + "/{customerId}") { backStackEntry ->
                val customerId =
                    backStackEntry.arguments?.getString("customerId")?.toLong() ?: return@composable
                val customer = viewModel.getCustomerById(customerId)

                if (customer == null) {
                    // Handle the case where the customer is not found
                    Text("Customer not found")
                } else {
                    val productList by viewModel.productList.collectAsState()

                    ProductSelectionScreen(
                        customer = customer,
                        onProceedToCart = {
                            // Navigate to the Cart screen with the customer ID
                            navController.navigate(Destinations.Cart.route + "/${customer.customerId}")
                        },
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }


// Cart Screen Selection
            composable(Destinations.Cart.route + "/{customerId}") { backStackEntry ->
                val customerId =
                    backStackEntry.arguments?.getString("customerId")?.toLong() ?: return@composable
                val customer = viewModel.getCustomerById(customerId) // Fetch customer logic

                // Ensure we fetch cart items if they are not already available
                LaunchedEffect(customerId) {
                    viewModel.refreshCartItems(customerId)
                }

                if (customer != null) {
                    CartScreen(
                        customer = customer,
                        navController,
                        viewModel
                    )

                }
            }

            composable(Destinations.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    navController
                )
            }

//            LoadStocks screen
            composable(Destinations.LoadStocks.route) {
                LoadStockScreen(
                    viewModel = viewModel,
                    navController,
                    {}
                )
            }
//
            composable(Destinations.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel,
                    onInvoiceClick = { invoiceLine ->
                        navController.navigate(Destinations.InvoiceDetail.createRoute(invoiceLine.invoiceID))
                    }
                )
            }
            // For Invoice Detail Screen
            composable(
                route = Destinations.InvoiceDetail.route,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId")
                invoiceId?.let {
                    // Show the InvoiceDetailScreen for the given invoiceId
                    InvoiceDetailScreen(invoiceId = it, viewModel = viewModel)
                }
            }

            // Order Screen
            composable(Destinations.Order.route) {
                OrderScreen(onSubmit = { productName, quantity, dueDate, notes ->
                    // Handle order submission logic
                    viewModel.submitOrder(productName, quantity, dueDate, notes)
                    // Navigate back or to another screen
                    navController.popBackStack()
                })
            }

            composable(Destinations.OrderReports.route) {
                OrdersReportsScreen(
                    viewModel = viewModel,
                    onNewOrderClick = { navController.navigate(Destinations.Order.route) }
                )
            }
//

        }

    }
}

@Composable
fun IndriveDrawerNav(navController: NavController, scaffoldState: ScaffoldState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyGray)
    ) {
        // Make the header and body scrollable and take up remaining space
        Column(
            modifier = Modifier
                .weight(1f)  // Ensures the drawer content takes all available space
                .verticalScroll(rememberScrollState())  // Makes it scrollable if content is large
                .padding(8.dp)
        ) {
            IndriveDrawerHeader()
            IndriveDrawerBody(navController = navController, scaffoldState)
        }

        // IndriveDrawerBottom is at the bottom, outside the scrollable area
        IndriveDrawerBottom(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun IndriveDrawerHeader() {
    val userImage = painterResource(id = R.drawable.user)

    Column(modifier = Modifier.background(MyGray)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            Image(
                painter = userImage, contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .clip(CircleShape)
            )

            // Column containing text, star icons, and rating
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.myusername),
                    color = Color.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star rating",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Yellow
                        )
                    }
                    Text(
                        text = " 4.7",
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Text(
                        text = " (101) ",
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(22.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun IndriveDrawerBody(navController: NavController, scaffoldState: ScaffoldState) {
    val coroutineScope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel()

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Rides.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "City", color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Wallet.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "Wallet", color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Priority.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "City to City", color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Income.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "Safety", color = Color.Black)
        }
//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Rides.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "Settings", color = Color.Black)
        }
//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Priority.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "FAQ", color = Color.Black)
        }
//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Income.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Face, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "Support", color = Color.Black)
        }
//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    authViewModel.signout()
                    navController.navigate(Destinations.SignIn.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = "Signout", color = Color.Black)
        }
//
    }
}

@Composable
fun IndriveDrawerBottom(modifier: Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Horizontal divider
        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

// Lime Green button for Passenger mode
        Button(
            onClick = { /* Handle button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(Color(0xff66cc00)) // Lime Green color
        ) {
            Text(
                text = "Passenger mode",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }


        // Row with Facebook and Instagram icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { /* Handle Facebook click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.facebook), // Replace with your Facebook icon resource
                    contentDescription = "Facebook",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { /* Handle Instagram click */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.instagram), // Replace with your Instagram icon resource
                    contentDescription = "Instagram",
                    tint = Color(0xFFC13584), // Instagram-like color
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun IndriveBottomNavigation(navController: NavHostController) {
    BottomNavigation(
        backgroundColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Rides.route,
            onClick = { navController.navigate(Destinations.Rides.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(text = Destinations.Rides.route, color = Color.Black) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Income.route,
            onClick = { navController.navigate(Destinations.Income.route) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(text = Destinations.Income.route, color = Color.Black) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Priority.route,
            onClick = { navController.navigate(Destinations.Priority.route) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(text = Destinations.Priority.route, color = Color.Black) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Wallet.route,
            onClick = { navController.navigate(Destinations.Wallet.route) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(text = Destinations.Wallet.route, color = Color.Black) }
        )
    }
}
