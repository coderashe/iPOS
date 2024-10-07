package com.example.learncompose303.app

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Switch
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learncompose303.R
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.screens.CartScreen
import com.example.learncompose303.screens.CustomerSelectionScreen
import com.example.learncompose303.screens.InvoiceDetailScreen
import com.example.learncompose303.screens.LoadStockScreen
import com.example.learncompose303.screens.LoginScreen
import com.example.learncompose303.screens.MyIncomeScreen
import com.example.learncompose303.screens.OrderScreen
import com.example.learncompose303.screens.OrdersReportsScreen
import com.example.learncompose303.screens.PriorityScreen
import com.example.learncompose303.screens.ProductSelectionScreen
import com.example.learncompose303.screens.ProductsScreen
import com.example.learncompose303.screens.ReportsScreen
import com.example.learncompose303.screens.RideRequestsScreen
import com.example.learncompose303.screens.SignInPage
import com.example.learncompose303.screens.SignUpPage
import com.example.learncompose303.screens.WalletScreen
import com.example.learncompose303.screens.auth.AuthState
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.ui.theme.MyBlueish
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.viewmodels.InvoiceViewModel
import kotlinx.coroutines.launch


@Composable
fun DefaultScreen() {
    Surface {
// TODO:    Test ui here
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InDriveTopBottomNavigation() {
    val authViewModel: AuthViewModel = viewModel() // Get AuthViewModel
    val viewModel: InvoiceViewModel = viewModel()
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var isChecked by remember { mutableStateOf(false) }

    // Observe auth state and navigate to sign-in page when unauthenticated
    val authState by authViewModel.authState.observeAsState()

    // Navigate to sign-in when user is unauthenticated
    LaunchedEffect(authState) {
        if (authState == AuthState.UnAuthenticated) {
            navController.navigate(Destinations.SignIn.route) {
                popUpTo(Destinations.SignIn.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { authViewModel.onUserInteraction() }, // Reset timer on user interaction
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Determine if the user is authenticated
                        val isChecked =
                            authState == AuthState.Authenticated // Set Switch state based on authState

                        Text(
                            text = if (isChecked) "Online" else "Offline",
                            modifier = Modifier.padding(end = 8.dp),
                            color = if (isChecked) Color.Green else Color.Red
                        )
                        Switch(
                            checked = isChecked,
                            onCheckedChange = {
                                // Do not change auth state through switch for now, if needed implement functionality.
                                authViewModel.onUserInteraction() // Reset timer on interaction
                            }
                        )

                    }
                },
                backgroundColor = Color.White,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch { scaffoldState.drawerState.open() }
                            authViewModel.onUserInteraction() // Reset timer on interaction
                        }
                    ) {
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
        // Reset logout timer on interaction with main content
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable { authViewModel.onUserInteraction() } // Reset timer on interaction
        ) {
            // Set the starting destination of the app
            NavHost(
                navController = navController,
                startDestination = Destinations.SignIn.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Destinations.SignUp.route) {
                    SignUpPage(navController, authViewModel)
                }
                composable(Destinations.SignIn.route) {
                    SignInPage(navController, authViewModel)
                }
                composable(Destinations.Rides.route) {
                    RideRequestsScreen(navController, viewModel)
                }
                composable(Destinations.Income.route) {
                    MyIncomeScreen(viewModel, navController)
                }
                composable(Destinations.Priority.route) {
                    PriorityScreen(viewModel)
                }
                composable(Destinations.Wallet.route) {
                    WalletScreen()
                }
                composable(Destinations.Products.route) {
                    ProductsScreen(viewModel)
                }

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
                        backStackEntry.arguments?.getString("customerId")?.toLong()
                            ?: return@composable
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

                // Cart Screen
                composable(Destinations.Cart.route + "/{customerId}") { backStackEntry ->
                    val customerId =
                        backStackEntry.arguments?.getString("customerId")?.toLong()
                            ?: return@composable
                    val customer = viewModel.getCustomerById(customerId)

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
                    LoginScreen(viewModel = viewModel, navController)
                }

                composable(Destinations.LoadStocks.route) {
                    LoadStockScreen(viewModel = viewModel, navController) {}
                }

                // Navigation for ReportsScreen composable
                composable(Destinations.Reports.route) {
                    ReportsScreen(
                        viewModel = viewModel,
                        onInvoiceClick = { invoiceId ->
                            Log.e(
                                "istartlogs",
                                "Navigating to InvoiceDetailScreen with invoiceId: $invoiceId"
                            ) // Log here to check
                            navController.navigate(
                                Destinations.InvoiceDetail.createRoute(invoiceId)  // Use invoiceId
                            )
                        }
                    )
                }

                composable(
                    route = Destinations.InvoiceDetail.route,
                    arguments = listOf(navArgument("invoiceId") {
                        type = NavType.LongType
                    })  // Declare nav argument
                ) { backStackEntry ->

                    val invoiceId = backStackEntry.arguments?.getLong("invoiceId")
                    Log.e("istartlogs", "inside InvoiceDetailScreen with invoiceId: $invoiceId")

                    invoiceId?.let {
                        InvoiceDetailScreen(invoiceId = it, viewModel = viewModel)
                    }
                }

                composable(Destinations.Order.route) {
                    OrderScreen(onSubmit = { productName, quantity, dueDate, notes ->
                        viewModel.submitOrder(productName, quantity, dueDate, notes)
                        navController.popBackStack()
                    })
                }

                composable(Destinations.OrderReports.route) {
                    OrdersReportsScreen(viewModel = viewModel) {
                        navController.navigate(Destinations.Order.route)
                    }
                }
            }
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
                .padding(8.dp),
            navController
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
            Text(text = stringResource(id = R.string.home), color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.OrderReports.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = stringResource(id = R.string.orders1), color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Reports.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = stringResource(id = R.string.reports1), color = Color.Black)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.LoadStocks.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = stringResource(id = R.string.load_stocks1), color = Color.Black)
        }
//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.CustomerSelection.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = stringResource(id = R.string.customers), color = Color.Black)
        }
//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    navController.navigate(Destinations.Products.route)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = stringResource(id = R.string.products), color = Color.Black)
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
            Icon(Icons.Default.Face, contentDescription = null)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = stringResource(R.string.faq), color = Color.Black)
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
            Text(text = stringResource(id = R.string.sign_out), color = Color.Black)
        }
//
    }
}

@Composable
fun IndriveDrawerBottom(modifier: Modifier, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel()

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
            onClick = {
                authViewModel.signout()
                navController.navigate(Destinations.SignIn.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(MyBlueish) // Lime Green color
        ) {
            Text(
                text = "Sign out",
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
            label = { Text(text = stringResource(id = R.string.navhome), color = Color.Black) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Income.route,
            onClick = { navController.navigate(Destinations.Income.route) },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.navincome), color = Color.Black) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.OrderReports.route,
            onClick = { navController.navigate(Destinations.OrderReports.route) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.navorders), color = Color.Black) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Products.route,
            onClick = { navController.navigate(Destinations.Products.route) },
            icon = { Icon(Icons.Default.ProductionQuantityLimits, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.navproducts), color = Color.Black) }
        )
    }
}
