package com.example.learncompose303.data.domain.util

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.screens.CartScreen
import com.example.learncompose303.viewmodels.InvoiceViewModel


//        override suspend fun insertInvoice(invoice: Invoice): Long {
//            return withContext(Dispatchers.IO) {
//                invoiceDao.insertInvoice(invoice)
//            }
//        }

//    override suspend fun updateInvoice(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.updateInvoice(invoice)
//        }
//    }
//
//    override suspend fun deleteInvoice(invoice: Invoice) {
//        return withContext(Dispatchers.IO) {
//            invoiceDao.deleteInvoice(invoice)
//        }
//    }

//
//
////                    DefaultScreen()
////                    FacebookScaffold(navController)
////                    SelectImage()
////                    val itemViewModel: ItemViewModel = viewModel()
////                    ItemListScreen(viewModel = itemViewModel)
//
////                    FingerprintAuthenticationScreen()
//
//
//fun isBiometricAuthenticationSupported(context: Context): Boolean {
//    val biometricManager = BiometricManager.from(context)
//    return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
//}
//
//class BiometricAuthentication(activity: FragmentActivity) {
//    private val biometricPrompt: BiometricPrompt by lazy {
//        val executor = ContextCompat.getMainExecutor(activity)
//        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
//            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                super.onAuthenticationError(errorCode, errString)
//                // Handle error
//            }
//
//            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                super.onAuthenticationSucceeded(result)
//                // Authentication succeeded
//            }
//
//            override fun onAuthenticationFailed() {
//                super.onAuthenticationFailed()
//                // Authentication failed
//            }
//        })
//    }
//
//    fun authenticate(cryptoObject: BiometricPrompt.CryptoObject? = null) {
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Authentication")
//            .setSubtitle("Scan your fingerprint to proceed")
//            .setNegativeButtonText("Cancel")
//            .build()
//        if (cryptoObject != null) {
//            biometricPrompt.authenticate(promptInfo, cryptoObject)
//        }
//    }
//}
//
//@Composable
//fun FingerprintAuthenticationScreen() {
//    val context = LocalContext.current
//    val activity = context as FragmentActivity
//    val biometricAuthentication = remember { BiometricAuthentication(activity) }
//
//    Column {
//        Text("Fingerprint Authentication")
//        if (isBiometricAuthenticationSupported(context)) {
//            Button(onClick = { biometricAuthentication.authenticate() }) {
//                Text("Authenticate with Fingerprint")
//            }
//        } else {
//            Text("Biometric authentication is not supported or configured on this device.")
//        }
//    }
//}


// RidesScreen()

//for indrive

//    val searchImage = painterResource(id = R.drawable.searching)

//        Column(
//            modifier = Modifier
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = stringResource(id = R.string.rides),
//                color = Color.Black,
//                fontSize = 16.sp,
//                modifier = Modifier
//                    .padding(8.dp)
//            )
//            Image(
//                painter = searchImage, contentDescription = null,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(4.dp),
//                contentScale = ContentScale.Fit // Crop and scale the image to the center
//            )
//        }

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CustomWidthToggleSwitch() {
//    var isChecked by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .width(120.dp), // Set the width to 120.dp
//        contentAlignment = Alignment.Center
//    ) {
//        // Switch component with custom width
//        Switch(
//            checked = isChecked,
//            onCheckedChange = { isChecked = it },
//            modifier = Modifier
//                .fillMaxWidth() // Ensure switch takes full width
//                .width(200.dp)
//        )
//
//        // Text overlay
//        Text(
//            text = if (isChecked) "ON" else "OFF",
//            color = Color.White,
//            fontWeight = FontWeight.Bold,
//            fontSize = 16.sp
//        )
//    }
//}

// InvoicingScreen
//
//@Composable
//fun ProductList(products: List<Product>, onAddToCart: () -> Unit) {
//    LazyColumn(
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(products.size) { index ->
//            ProductItem(product = products[index], onAddToCart = onAddToCart)
//        }
//    }
//}
//
//@Composable
//fun ProductItem(product: Product, onAddToCart: () -> Unit) {
//    Card(
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .width(150.dp)
//            .height(220.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                // Product Image
//                Image(
//                    painter = painterResource(id = product.imageResId),
//                    contentDescription = product.name,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(120.dp)
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Text(
//                    modifier = Modifier.padding(8.dp),
//                    text = product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                ) {
//                    Text(
//                        modifier = Modifier.weight(1f),
//                        text = product.price,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 16.sp,
//                        color = Color.Gray
//                    )
//                    // Add to Cart Button
//                    IconButton(
//                        onClick = onAddToCart,
//                        modifier = Modifier
//                            .size(40.dp)
//                            .background(Green1, shape = CircleShape)
//                            .padding(8.dp),
//                    ) {
//                        Icon(
//                            painter = painterResource(id = android.R.drawable.ic_input_add),
//                            contentDescription = "Add to Cart",
//                            tint = Color.White
//                        )
//                    }
//
//                }
//            }
//
//        }
//    }
//}

// from DefaultScreen

//@Composable
//fun NavigationAppHost(navController: NavHostController) {
//    NavHost(navController = navController, startDestination = "home") {
//        composable(Destinations.Home.route) { HomeScreen() }
//        composable(Destinations.Post.route) { PostScreen() }
//        composable(Destinations.Search.route) { SearchScreen() }
//        composable(Destinations.TTTScreen.route) { TTTScreen(navController) }
//    }
//}

//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun TheBottomNavigation(navController: NavHostController) {
//    val scaffoldState = rememberScaffoldState()
//    val coroutineScope = rememberCoroutineScope()
//
//    Scaffold(
//        scaffoldState = scaffoldState,
//        topBar = {
//            TopAppBar(
//                title = { Text(text = "ISTART STORE") },
//                navigationIcon = {
//                    IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
//                        Icon(Icons.Default.Menu, contentDescription = null)
//                    }
//                }
//            )
//        },
//        drawerContent = {
//            TestDrawerNav(navController = navController, scaffoldState)
//        },
//        bottomBar = {
//            TestBottomNavigation(navController)
//        },
//    ) { paddingValues ->
//        NavHost(
//            navController = navController,
//            startDestination = Destinations.Home.route,
//            modifier = Modifier.padding(paddingValues)
//        ) {
//            composable(Destinations.Home.route) { HomeScreen() }
//            composable(Destinations.Post.route) { PostScreen() }
//            composable(Destinations.Search.route) { SearchScreen() }
//            composable(Destinations.TTTScreen.route) { TTTScreen(navController) }
//        }
//    }
//}
//
//@Composable
//fun TestDrawerNav(navController: NavController, scaffoldState: ScaffoldState) {
//    DrawerHeader()
//    DrawerBoady(navController = navController, scaffoldState)
//}
//
//@Composable
//fun DrawerHeader() {
//    val headerImage = painterResource(id = R.drawable.loc1)
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(MyGray)
//            .padding(64.dp)
//    ) {
//        Image(
//            painter = headerImage,
//            contentDescription = null
//        )
//    }
//}

//@Composable
//fun DrawerBoady(navController: NavController, scaffoldState: ScaffoldState) {
//    val coroutineScope = rememberCoroutineScope()
//
//    Column {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .clickable {
//                    navController.navigate(Destinations.Home.route)
//                    coroutineScope.launch { scaffoldState.drawerState.close() }
//                }
//        ) {
//            Icon(Icons.Default.Home, contentDescription = null)
//            Spacer(modifier = Modifier.width(20.dp))
//            Text(text = "HOME")
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .clickable {
//                    navController.navigate(Destinations.TTTScreen.route)
//                    coroutineScope.launch { scaffoldState.drawerState.close() }
//                }
//        ) {
//            Icon(Icons.Default.Favorite, contentDescription = null)
//            Spacer(modifier = Modifier.width(20.dp))
//            Text(text = "TTTScreen")
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .clickable {
//                    navController.navigate(Destinations.Post.route)
//                    coroutineScope.launch { scaffoldState.drawerState.close() }
//                }
//        ) {
//            Icon(Icons.Default.PlayArrow, contentDescription = null)
//            Spacer(modifier = Modifier.width(20.dp))
//            Text(text = "Post")
//        }
//    }
//}

//@Composable
//fun TestBottomNavigation(navController: NavHostController) {
//    BottomNavigation {
//        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentDestination = navBackStackEntry?.destination
//
//        BottomNavigationItem(
//            selected = currentDestination?.route == Destinations.Home.route,
//            onClick = { navController.navigate(Destinations.Home.route) },
//            icon = { Icon(Icons.Default.Home, contentDescription = null) },
//            label = { Text(text = Destinations.Home.route) }
//        )
//
//        BottomNavigationItem(
//            selected = currentDestination?.route == Destinations.TTTScreen.route,
//            onClick = { navController.navigate(Destinations.TTTScreen.route) },
//            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
//            label = { Text(text = Destinations.TTTScreen.route) }
//        )
//
//        BottomNavigationItem(
//            selected = currentDestination?.route == Destinations.Post.route,
//            onClick = { navController.navigate(Destinations.Post.route) },
//            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
//            label = { Text(text = Destinations.Post.route) }
//        )
//    }
//}

//@Composable
//fun DefaultScreen() {
//    Surface {
////        ButtonComponents()
////        DisplayYoutubeData()
////        LearnCheckBoxes()
////        LearnFAB()
////        LearnExtendedFAB()
////        LearnState()
////        LearnTopAppBar()
////        LearnnavDrawer()
////        InstagramProfile()
////        TestLazyLists()
////        YotubeVideoList()
////        TTTScreen()
//
////        val cities = remember { mutableListOf<City>() }
////        val sort = remember { mutableStateOf(false) }
////        val sortedCities = if (sort.value) cities.sortedBy { it.name[0] } else cities
////
////        CitiesList(
////            sortedCities,
////            onAddCity = { name, country ->
////                cities.add(City(name, country))
////            },
////            onSort = {
////                sort.value = !sort.value
////            }
////        )
//
////        InDriveTopBottomNavigation(navController)
//
//
////        val navController = rememberNavController()
////        NavigationAppHost(navController = navController)
////        TheBottomNavigation(navController)
//
//    }
//}

//
//@Composable
//fun InDriveTopBottomNavigation(navController: NavHostController) {
//    val scaffoldState = rememberScaffoldState()
//    val coroutineScope = rememberCoroutineScope()
//    var isChecked by remember { mutableStateOf(false) }
//
//    Scaffold(
//        scaffoldState = scaffoldState,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(), // Fill entire top bar width
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center // Center content horizontally
//                    ) {
//                        Text(
//                            text = if (isChecked) "Online" else "Offline",
//                            modifier = Modifier.padding(end = 8.dp),
//                            color = if (isChecked) Color.Green else Color.Red // Green for "Online" and Red for "Offline"
//                        )
//                        Switch(
//                            checked = isChecked,
//                            onCheckedChange = { isChecked = it }
//                        )
//                    }
//
//                },
//                backgroundColor = Color.White,
//                navigationIcon = {
//                    IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
//                        Icon(Icons.Default.Menu, contentDescription = null)
//                    }
//                }
//            )
//        },
//        drawerContent = {
//            IndriveDrawerNav(navController = navController, scaffoldState)
//        },
//        bottomBar = {
//            IndriveBottomNavigation(navController)
//        },
//    ) { paddingValues ->
//        NavHost(
//            navController = navController,
//            startDestination = Destinations.Rides.route,
//            modifier = Modifier.padding(paddingValues)
//        ) {
//            composable(Destinations.Rides.route) { RideRequestsScreen(navController) }
//            composable(Destinations.Income.route) { MyIncomeScreen() }
//            composable(Destinations.Priority.route) { PriorityScreen() }
//            composable(Destinations.Wallet.route) { WalletScreen() }
//            composable(Destinations.Invoice.route) { InvoicingScreen() }
//        }
//    }
//}

//mainActivity

//                val viewModel: InvoiceViewModel = viewModel()
//                val navController = rememberNavController()
//                val viewModel: HomeViewModel = viewModel()
//                    HomeScreen()
//                    LoginScreen(viewModel = viewModel)

//@Composable
//fun CartScreen(
//    customer: Customer,
//    navController: NavController,
//    viewModel: InvoiceViewModel = viewModel()
//) {
//    // Get the selected products
//    val selectedProducts by viewModel.selectedProducts.collectAsState()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Display the list of selected products
//        LazyColumn {
//            items(selectedProducts) { product ->
//                var quantity by remember { mutableStateOf(1) } // State to store quantity
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                ) {
//                    Text(product.name, modifier = Modifier.weight(1f))
//                    Text("Price: \$${product.price}", modifier = Modifier.weight(1f))
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    // Input for quantity
//                    TextField(
//                        value = quantity.toString(),
//                        onValueChange = { newValue -> quantity = newValue.toIntOrNull() ?: 1 }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(
//            onClick = {
//                navController.navigate(Destinations.Rides.route) // Navigate to InvoiceScreen
//            },
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text("Finalize Invoice")
//        }
//    }
//}

// old button
//        Button(
//            onClick = {
//                val tenderedAmount = amountTendered.toDoubleOrNull()
//
//                if (amountTendered.isNotEmpty() && tenderedAmount != null && tenderedAmount > 0) {
//                    if (tenderedAmount >= totalPrice) {
//                        // Submit the invoice and capture cart item details
//                        viewModel.submitInvoice(
//                            customer.customerId,
//                            tenderedAmount,
//                            productQuantities = quantitySold
//                        )
//
//                        // Your code for sharing the invoice message...
//
//                        // Create an intent to send the message
//                        val message = "*INVOICE RECEIPT:*\n\n" +
//                                "*CUSTOMER DETAILS*\n" +
//                                "${customer.name}\n" +
//                                "${customer.phone}\n\n" +
//                                "*PRODUCTS PURCHASED:*\n" +
//                                "$productDetails\n\n" +
//                                "*SALES TOTALS*\n" +
//                                "Total Amount: *$$totalPrice*\n" +
//                                "Amount Tendered: *$$amountTendered*\n" +
//                                "Change: *$${amountTendered.toDouble() - totalPrice}*\n\n" +
//                                "Thank you for your purchase!...\n              ISTARTK  \u00A9 2024"
//
//                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
//                            type = "text/plain"
//                            putExtra(Intent.EXTRA_TEXT, message)
//                        }
//
//                        try {
//                            // Use a chooser to select WhatsApp or WhatsApp Business and send message
//                            val chooserIntent = Intent.createChooser(
//                                sendIntent,
//                                "Send via WhatsApp or WhatsApp Business"
//                            )
//                            chooserIntent.resolveActivity(context.packageManager)?.let {
//                                context.startActivity(chooserIntent)
//                            } ?: run {
//                                Toast.makeText(
//                                    context,
//                                    "No messaging app found",
//                                    Toast.LENGTH_SHORT
//                                )
//                                    .show()
//                            }
//                        } catch (e: Exception) {
//                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
//                                .show()
//                        }
//
//                        // Navigate to the receipt screen or another destination after submitting the invoice
//                        navController.navigate(Destinations.Rides.route) // Replace this with your actual destination for receipt
//                        Toast.makeText(context, "Invoice paid successfully", Toast.LENGTH_SHORT)
//                            .show()
//                    } else {
//                        Toast.makeText(
//                            context,
//                            "Amount tendered must be greater than or equal to total price",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } else {
//                    Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            Text("Submit Invoice")
//        }


//
//LazyColumn {
//    items(selectedProducts) { product ->
//
//        // Find the product price from the collected productWithPriceDetails
//        val productPrice =
//            productWithPriceDetails.find { it.first.productId == product.productId }?.second
//                ?: 0.0
//
//        // Get the current quantity for this product from the map, default to 1 if not present
//        newQuantitySold = productQuantities[product.productId] ?: quantitySold
//
//        // Calculate the total price for the product
//        val productTotalPrice = productPrice * newQuantitySold
//
//        LaunchedEffect(quantitySold) {
//            productQuantities[product.productId] = newQuantitySold
//        }
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(2.dp),
//            elevation = CardDefaults.cardElevation(4.dp),
//            shape = RoundedCornerShape(8.dp),
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(4.dp),
//            ) {
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(
//                        product.productDescription,
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Text(
//                        "\$${productPrice}",
//                        style = MaterialTheme.typography.titleSmall
//                    )
//                }
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Column(horizontalAlignment = Alignment.End) {
//
//                    // Input for quantity with better handling
//                    OutlinedTextField(
//                        value = if (newQuantitySold == 0) "" else newQuantitySold.toString(),
//                        onValueChange = { newValue ->
//                            val newQuantity = newValue.toIntOrNull()?.takeIf { it > 0 } ?: 1
//                            productQuantities[product.productId] = newQuantity
//                        },
//                        label = { Text("Quantity") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        shape = RoundedCornerShape(8.dp)
//                    )
//
//                    Text(
//                        text = "SubTotal: \$${productTotalPrice}",
//                        color = MaterialTheme.colorScheme.primary,
//                        style = MaterialTheme.typography.titleSmall
//                    )
//                }
//            }
//        }
//
//        quantitySold = newQuantitySold
//        Log.d("CheckingQuantity", "Quantity for each product is $quantitySold")
//    }
//}


//    private val _productWithPriceDetails =
//        MutableStateFlow<List<Pair<Product, Double>>>(emptyList())
//    val productWithPriceDetails: StateFlow<List<Pair<Product, Double>>> = _productWithPriceDetails

//    // Function to get PriceList by productId
//    fun getPriceDetailsForSelectedProducts(selectedProducts: List<Product>) {
//        viewModelScope.launch {
//            val productDetailsWithPrices = selectedProducts.map { product ->
//                val priceList = priceListRepository.getPriceListByProductId(product.productId)
//                val productPrice = priceList?.sellingPrice ?: 0.0
//                product to productPrice
//            }
//            _productWithPriceDetails.value = productDetailsWithPrices
//        }
//    }


//    old 2 worked with deducting Stock
//    fun submitInvoice(customerId: Long, amountTendered: Double, quantitySold: Int) =
//        viewModelScope.launch {
//            try {
//                var totalAmount = 0.0
//                val taxPercentage = 0.15 // Example tax percentage
//                val discountPercentage = 0.0 // Example discount percentage
//
//                // Log the start of the operation
//                Log.d("Invoice", "Starting invoice submission for customerId: $customerId")
//
//                // Fetch customer details
//                val customer = customerRepository.getCustomerById(customerId)
//                if (customer == null) {
//                    Log.e("InvoiceError", "Customer not found for customerId: $customerId")
//                    return@launch
//                }
//
//                // Fetch selected products and their prices from PriceList, then calculate total amount
//                val selectedProductsWithDetails = _selectedProducts.value.map { product ->
//                    // Fetch the corresponding price from the PriceList entity
//                    val priceList = priceListRepository.getPriceListByProductId(product.productId)
//                    val basePrice = priceList?.sellingPrice ?: 0.0
//                    val taxAmount = basePrice * taxPercentage
//                    val discountAmount = basePrice * discountPercentage
//
////                    val finalPrice = basePrice + taxAmount - discountAmount
//                    val finalPrice = basePrice
//                    totalAmount += finalPrice * quantitySold // Use product.quantity for calculations
//
//                    product to finalPrice // Pair product with calculated final price
//                }
//
//                // Calculate change
//                val change = amountTendered - totalAmount
//                if (change < 0) {
//                    Log.e("InvoiceError", "Amount tendered is less than total amount!")
//                    return@launch
//                }
//
//                // Only proceed if there are valid selected products and totalAmount > 0
//                if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
//                    // Create an Invoice object
//                    val invoice = Invoice(
//                        customerId = customerId,
//                        totalAmount = totalAmount,
//                        invoiceDate = System.currentTimeMillis(),
//                        dueDate = null,
//                        status = "Paid"
//                    )
//
//                    // Insert the invoice and get the invoiceId
//                    val invoiceId = invoiceRepository.insertInvoice(invoice)
//
//                    Log.d("Invoice", "Inserted Invoice with id: $invoiceId")
//
//                    // Insert CartItems, InvoiceItems, and deduct stock for each selected product
//                    selectedProductsWithDetails.forEach { (product, finalPrice) ->
//
//                        // Deduct the sold quantity from stock
//                        deductQuantityAndUpdateStock(product.productId, quantitySold)
//
//                        // Create CartItem record
//                        val cartItem = CartItem(
//                            customerId = customerId,
//                            productId = product.productId,
//                            invoiceId = invoiceId,
//                            totalAmount = finalPrice * quantitySold,
//                            totalAmountTendered = amountTendered,
//                            change = change
//                        )
//                        cartItemRepository.insertCartItem(cartItem)
//
//                        // Create InvoiceItem record
//                        val invoiceItem = InvoiceItem(
//                            invoiceId = invoiceId,
//                            productId = product.productId,
//                            customerId = customerId,
//                            quantity = quantitySold.toInt(),
//                            price = finalPrice,
//                            total = finalPrice * quantitySold
//                        )
//                        invoiceItemRepository.insertInvoiceItem(invoiceItem)
//                    }
//
//                    // Insert InvoiceLine records for the report
//                    selectedProductsWithDetails.forEach { (product, finalPrice) ->
//                        val invoiceLine = InvoiceLine(
//                            invoiceID = invoiceId,
//                            invoiceStatus = invoice.status,
//                            productID = product.productId,
//                            productName = product.productDescription,
//                            productPrice = finalPrice,
//                            productQtySold = quantitySold.toInt(),
//                            customerID = customerId,
//                            customerName = customer.name,
//                            customerPhone = customer.phone,
//                            totalAmount = totalAmount,
//                            amountTendered = amountTendered,
//                            changeBalance = change,
//                            invoiceDate = System.currentTimeMillis()
//                        )
//                        invoiceLineRepository.insertInvoiceLine(invoiceLine)
//                    }
//
//                    // Clear the selected products list after invoice submission
//                    _selectedProducts.value = emptyList()
//                    Log.d("Invoice", "Cleared selected products for customer: $customerId")
//                } else {
//                    Log.e(
//                        "InvoiceError",
//                        "No valid selected products to invoice or total amount is 0"
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e("InvoiceError", "Error during invoice submission: ${e.message}")
//            }
//        }


//    old submit which was working fine
//    fun submitInvoice(customerId: Long, amountTendered: Double) = viewModelScope.launch {
//        try {
//            var totalAmount = 0.0
//            val taxPercentage = 0.15 // Example tax percentage
//            val discountPercentage = 0.0 // Example discount percentage
//
//            // Log the start of the operation
//            Log.d("Invoice", "Starting invoice submission for customerId: $customerId")
//
//            // Fetch customer details
//            val customer = customerRepository.getCustomerById(customerId)
//            if (customer == null) {
//                Log.e("InvoiceError", "Customer not found for customerId: $customerId")
//                return@launch
//            }
//
//            // Fetch selected products and their prices from PriceList, then calculate total amount
//            val selectedProductsWithDetails = _selectedProducts.value.map { product ->
//                // Fetch the corresponding price from the PriceList entity
//                val priceList = priceListRepository.getPriceListByProductId(product.productId)
//                val basePrice = priceList?.sellingPrice ?: 0.0
//                val taxAmount = basePrice * taxPercentage
//                val discountAmount = basePrice * discountPercentage
//                val finalPrice = basePrice + taxAmount - discountAmount
//                totalAmount += finalPrice * 1 // Assuming `product.quantity` is available
//
//                product to finalPrice // Pair product with calculated final price
//            }
//
//            // Calculate change
//            val change = amountTendered - totalAmount
//            if (change < 0) {
//                Log.e("InvoiceError", "Amount tendered is less than total amount!")
//                return@launch
//            }
//
//            // Only proceed if there are valid selected products and totalAmount > 0
//            if (selectedProductsWithDetails.isNotEmpty() && totalAmount > 0) {
//                // Create an Invoice object
//                val invoice = Invoice(
//                    customerId = customerId,
//                    totalAmount = totalAmount,
//                    invoiceDate = System.currentTimeMillis(),
//                    dueDate = null,
//                    status = "Paid"
//                )
//
//                // Insert the invoice and get the invoiceId
//                val invoiceId = invoiceRepository.insertInvoice(invoice)
//
//                Log.d("Invoice", "Inserted Invoice with id: $invoiceId")
//
//                // Insert CartItems and InvoiceItems for each selected product
//                selectedProductsWithDetails.forEach { (product, finalPrice) ->
//                    val cartItem = CartItem(
//                        customerId = customerId,
//                        productId = product.productId,
//                        invoiceId = invoiceId,
//                        totalAmount = finalPrice, // Final price after tax and discount
//                        totalAmountTendered = amountTendered,
//                        change = change
//                    )
//                    cartItemRepository.insertCartItem(cartItem)
//
//                    val invoiceItem = InvoiceItem(
//                        invoiceId = invoiceId,
//                        productId = product.productId,
//                        customerId = customerId,
//                        quantity = 1, // Quantity of the product
//                        price = finalPrice,
//                        total = finalPrice * 1
//                    )
//                    invoiceItemRepository.insertInvoiceItem(invoiceItem)
//                }
//
//                // Insert InvoiceLine records for the report
//                selectedProductsWithDetails.forEach { (product, finalPrice) ->
//                    val invoiceLine = InvoiceLine(
//                        invoiceID = invoiceId,
//                        invoiceStatus = invoice.status,
//                        productID = product.productId,
//                        productName = product.productDescription,
//                        productPrice = finalPrice,
//                        productQtySold = 1,
//                        customerID = customerId,
//                        customerName = customer.name,
//                        customerPhone = customer.phone,
//                        totalAmount = totalAmount,
//                        amountTendered = amountTendered,
//                        changeBalance = change,
//                        invoiceDate = System.currentTimeMillis()
//                    )
//                    invoiceLineRepository.insertInvoiceLine(invoiceLine)
//                }
//
//                // Clear the selected products list after invoice submission
//                _selectedProducts.value = emptyList()
//                Log.d("Invoice", "Cleared selected products for customer: $customerId")
//            } else {
//                Log.e("InvoiceError", "No valid selected products to invoice or total amount is 0")
//            }
//        } catch (e: Exception) {
//            Log.e("InvoiceError", "Error during invoice submission: ${e.message}")
//        }
//    }

//

//@Preview(showBackground = true)
//@Composable
//fun PreviewCartScreen() {
//
//    val viewModel: InvoiceViewModel = viewModel()
//    val navController = rememberNavController()
//
//    // Sample data
//    val customer =
//        Customer(
//            customerId = 1,
//            name = "John Doe",
//            email = "john.doe@example.com",
//            "0769853534",
//            "State House"
//        )
//
//    CartScreen(
//        customer = customer,
//        navController,
//        viewModel
//    )
//}
