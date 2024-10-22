package com.example.learncompose303.data.domain.util

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.screens.CartScreen
import com.example.learncompose303.screens.ProductCard
import com.example.learncompose303.viewmodels.InvoiceViewModel


//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 8.dp)
//                .horizontalScroll(rememberScrollState()),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            //        for productCsvUpload
//            QuickAccessCard(
//                label = stringResource(R.string.income_statement),
//                subTitle = "--",
//                icon = Icons.Default.More,
//                onClick = {
//                    navController.navigate(Destinations.IncomeStatement.route)
//                }
//            )
//        }

//    val pricelistRequestFileLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            val inputStream = context.contentResolver.openInputStream(it)
//            inputStream?.let {
//                viewModel.uploadPriceListsFromCsv(it, context)
//            }
//        }
//    }

//    fun uploadPriceListsFromCsv(inputStream: InputStream, context: Context) {
//        viewModelScope.launch {
//            val parser = CsvParser()
//            val priceListsFromCsv = parser.parsePriceListCsv(inputStream)
//
//            // Fetch existing products for productId validation
//            val existingProducts = productRepository.getAllProductsOnce()
//
//            // Create a set of existing product IDs for quick lookup
//            val existingProductIds = existingProducts.map { it.productId }.toSet()
//
//            // Filter new price lists to only include those with valid productIds
//            val newPriceLists = priceListsFromCsv.filter { priceList ->
//                existingProductIds.contains(priceList.productId) // Include only if productId exists
//            }.distinctBy { it.productId } // Ensure no duplicate productId in new price lists
//
//            // Convert PriceListCsvModel to PriceList entity
//            val priceListEntities = newPriceLists.map {
//                PriceList(
//                    productId = it.productId,
//                    productDescription = it.productDescription,
//                    unitCost = it.unitCost,
//                    sellingPrice = it.sellingPrice,
//                    markupPercentage = it.markupPercentage,
//                    grossProfitPercentage = it.grossProfitPercentage
//                )
//            }
//
//            // Insert price lists into the database
//            if (priceListEntities.isNotEmpty()) {
//                val insertedIds = priceListRepository.insertPriceListsAll(priceListEntities)
//                // Notify user of successful upload
//                Toast.makeText(
//                    context,
//                    "${insertedIds.size} price lists uploaded successfully.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                Toast.makeText(context, "No valid price lists to upload.", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//    }

// working creditNote
//    fun reverseInvoice(invoiceId: Long) = viewModelScope.launch {
//        try {
//            // Fetch the invoice to be reversed
//            val invoice = invoiceRepository.getInvoiceById(invoiceId)
//            if (invoice == null) {
//                Log.e("CreditNoteError", "Invoice not found with ID: $invoiceId")
//                return@launch
//            }
//
//            // Collect the Flow of InvoiceItems associated with the invoice, but limit it to one emission using `take(1)`
//            invoiceItemRepository.getInvoiceItemsByInvoiceId(invoiceId)
//                .take(1) // This ensures only one collection happens and then it stops
//                .collect { invoiceItems ->
//                    if (invoiceItems.isEmpty()) {
//                        Log.e(
//                            "CreditNoteError",
//                            "No invoice items found for invoice ID: $invoiceId"
//                        )
//                        return@collect
//                    }
//
//                    // Reverse each item, restock the quantity, and create a reverse transaction
//                    invoiceItems.forEach { invoiceItem ->
//                        // Restore product stock by adding back the sold quantity
//                        addQuantityBackToStock(
//                            invoiceItem.productId,
//                            invoiceItem.quantity.toDouble()
//                        )
//
//                        // Create a credit note (or mark the original InvoiceItem as reversed)
//                        val creditNoteItem = InvoiceItem(
//                            invoiceId = invoiceId,
//                            productId = invoiceItem.productId,
//                            customerId = invoice.customerId,
//                            quantity = -invoiceItem.quantity, // Negative quantity to reverse
//                            price = invoiceItem.price,
//                            total = -invoiceItem.total // Negative total to reflect reversal
//                        )
//                        invoiceItemRepository.insertInvoiceItem(creditNoteItem)
//                    }
//
//                    // Mark the original invoice as "Reversed" or "Credit Note"
//                    val updatedInvoice = invoice.copy(
//                        status = "Credit Note",
//                        totalAmount = -invoice.totalAmount // Make the totalAmount negative
//                    )
//                    invoiceRepository.updateInvoice(updatedInvoice)
//
//                    // Fetch all InvoiceLines associated with the invoice
//                    val invoiceLine = invoiceLineRepository.getInvoiceLineById(invoiceId)
//
//                    // Insert reversed InvoiceLine records for the credit note
//                    invoiceLine?.let { line ->
//                        val creditNoteLine = InvoiceLine(
//                            invoiceId = invoiceId,
//                            invoiceStatus = "Credit Note",
//                            productID = line.productID,
//                            productName = line.productName,
//                            productPrice = line.productPrice,
//                            productQtySold = -line.productQtySold, // Negative quantity for reversal
//                            customerID = invoice.customerId,
//                            customerName = line.customerName,
//                            customerPhone = line.customerPhone,
//                            totalAmount = -invoice.totalAmount, // Negative totalAmount for reversal
//                            amountTendered = 0.0, // No money exchanged for a credit note
//                            changeBalance = 0.0,  // No change for a credit note
//                            invoiceDate = System.currentTimeMillis()
//                        )
//
//                        Log.d("CreditNote", "Inserting credit note line: $creditNoteLine")
//                        invoiceLineRepository.insertInvoiceLine(creditNoteLine)
//                    }
//
//                    Log.d("CreditNote", "Successfully reversed invoice ID: $invoiceId")
//                }
//        } catch (e: Exception) {
//            Log.e("CreditNoteError", "Error during credit note creation: ${e.message}")
//        }
//    }
//


//@Composable
//fun ProductSelectionScreen(
//    customer: Customer,
//    onProceedToCart: () -> Unit,
//    navController: NavController,
//    viewModel: InvoiceViewModel = viewModel()
//) {
//
//    LaunchedEffect(key1 = true) {
//        viewModel.getWalkinCustomer()
//    }
//    val products by viewModel.productList.collectAsState()
//    val selectedProducts by viewModel.selectedProducts.collectAsState() // Get selected products
//
//    val defaultCustomer = viewModel.getWalkinCustomer()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Row {
//            if (defaultCustomer != null) {
//                Text(
//                    text = "Customer: ${if (customer.name.isNullOrEmpty()) defaultCustomer.name else customer.name}",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp,
//                    modifier = Modifier.weight(1f)
//                )
//            }
//            Spacer(modifier = Modifier.width(16.dp))
//            Button(
//                onClick = onProceedToCart,
//            ) {
//                Text("Proceed to Cart")
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        LazyColumn {
//            items(products.chunked(3)) { rowItems ->
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    for (product in rowItems) {
//                        val isSelected = selectedProducts.contains(product)
//                        val price = viewModel.getPricelistById(product.productId)
//                        if (price != null) {
//                            ProductCard(
//                                product = product,
//                                isSelected = isSelected,
//                                onAddProduct = {
//                                    viewModel.addProductToCart(product)
//                                },
//                                price = price.sellingPrice
//                            )
//                        } else {
//                            Log.d(
//                                "HazyLogs",
//                                "No price found for product ID: ${product.productId}"
//                            )
//                        }
//                    }
//                    if (rowItems.size == 1) {
//                        Spacer(modifier = Modifier.weight(1f)) // Fill the space for uneven row
//                    }
//                }
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//    }
//}

// was working navigation


//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun RideRequestsScreen(
//    navController: NavHostController,
//    viewModel: InvoiceViewModel = viewModel()
//) {
//
//    val authViewModel: AuthViewModel = viewModel()
//    val authState = authViewModel.authState.observeAsState()
//
//    LaunchedEffect(authState.value) {
//        when (authState.value) {
//            is AuthState.UnAuthenticated -> navController.navigate(Destinations.SignIn.route)
//            else -> Unit
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Transparent)
//            .padding(top = 4.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//        SalesmanInfoCard(
//            salesmanName = "Simba Start",
//            shopName = "ISTART CAPITAL MRT",
//            viewModel = viewModel // Pass the viewModel explicitly
//        )
//        SpacerHeight()
////        SearchBarExample()
//        CustomHeadingMedium(
//            heading = stringResource(R.string.popular_products)
//        )
//        AutoSlidingCarousel()
//        CustomHeadingMedium(
//            heading = stringResource(R.string.more)
//        )
//        QuickAccessButtons(navController)
//    }
//}


//    fun syncFirestoreInvoicesWithRoom() {
//        val firestore = FirebaseFirestore.getInstance()
//
//        firestore.collection("invoices")
//            .addSnapshotListener { snapshots, e ->
//                if (e != null) {
//                    Log.w("FirestoreSync", "Listen failed", e)
//                    return@addSnapshotListener
//                }
//
//                if (snapshots != null && !snapshots.isEmpty) {
//                    val invoices = snapshots.documents.mapNotNull { doc ->
//                        val invoiceId = doc.getLong("invoiceId") ?: return@mapNotNull null
//                        val customerId = doc.getLong("customerId") ?: return@mapNotNull null
//                        val totalAmount = doc.getDouble("totalAmount") ?: return@mapNotNull null
//                        val invoiceDate = doc.getLong("invoiceDate") ?: return@mapNotNull null
//                        val dueDate = doc.getLong("dueDate")
//                        val status = doc.getString("status") ?: return@mapNotNull null
//                        val currencyCode = doc.getString("currencyCode") ?: return@mapNotNull null
//                        val lastUpdated = doc.getLong("lastUpdated")
//                        val isArchived = doc.getBoolean("isArchived") ?: false
//
//                        Invoice(
//                            invoiceId = invoiceId,
//                            customerId = customerId,
//                            totalAmount = totalAmount,
//                            invoiceDate = invoiceDate,
//                            dueDate = dueDate,
//                            status = status,
//                            currencyCode = currencyCode,
//                            lastUpdated = lastUpdated,
//                            isArchived = isArchived
//                        )
//                    }
//
//                    // Sync the list of invoices with Room
//
//                    viewModelScope.launch(Dispatchers.IO) {
//                        invoices.forEach { invoice ->
//                            invoiceRepository.insertInvoice(invoice)
//                        }
//                    }
//                }
//            }
//    }


//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun InDriveTopBottomNavigation() {
//    val authViewModel: AuthViewModel = viewModel() // Get AuthViewModel
//    val viewModel: InvoiceViewModel = viewModel()
//    val navController = rememberNavController()
//    val scaffoldState = rememberScaffoldState()
//    val coroutineScope = rememberCoroutineScope()
//    var isChecked by remember { mutableStateOf(false) }
//
//    // Observe auth state and navigate to sign-in page when unauthenticated
//    val authState by authViewModel.authState.observeAsState()
//
//    // Navigate to sign-in when user is unauthenticated
//    LaunchedEffect(authState) {
//        if (authState == AuthState.UnAuthenticated) {
//            navController.navigate(Destinations.SignIn.route) {
//                popUpTo(Destinations.SignIn.route) { inclusive = true }
//            }
//        }
//    }
//
//    Scaffold(
//        scaffoldState = scaffoldState,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { authViewModel.onUserInteraction() }, // Reset timer on user interaction
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = if (isChecked) "Online" else "Offline",
//                            modifier = Modifier.padding(end = 8.dp),
//                            color = if (isChecked) Color.Green else Color.Red
//                        )
//                        Switch(
//                            checked = isChecked,
//                            onCheckedChange = {
//                                isChecked = it
//                                authViewModel.onUserInteraction() // Reset timer on interaction
//                            }
//                        )
//                    }
//                },
//                backgroundColor = Color.White,
//                navigationIcon = {
//                    IconButton(
//                        onClick = {
//                            coroutineScope.launch { scaffoldState.drawerState.open() }
//                            authViewModel.onUserInteraction() // Reset timer on interaction
//                        }
//                    ) {
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
//        // Reset logout timer on interaction with main content
//        Box(modifier = Modifier
//            .fillMaxSize()
//            .clickable { authViewModel.onUserInteraction() } // Reset timer on interaction
//        ) {
//            // Set the starting destination of the app
//            NavHost(
//                navController = navController,
//                startDestination = Destinations.SignIn.route,
//                modifier = Modifier.padding(paddingValues)
//            ) {
//                composable(Destinations.SignUp.route) {
//                    SignUpPage(navController, authViewModel)
//                }
//                composable(Destinations.SignIn.route) {
//                    SignInPage(navController, authViewModel)
//                }
//                composable(Destinations.Rides.route) {
//                    RideRequestsScreen(navController, viewModel)
//                }
//                composable(Destinations.Income.route) {
//                    MyIncomeScreen(viewModel, navController)
//                }
//                composable(Destinations.Priority.route) {
//                    PriorityScreen(viewModel)
//                }
//                composable(Destinations.Wallet.route) {
//                    WalletScreen()
//                }
//                composable(Destinations.Products.route) {
//                    ProductsScreen(viewModel)
//                }
//
//                // Customer Selection Screen
//                composable(Destinations.CustomerSelection.route) {
//                    CustomerSelectionScreen(
//                        onCustomerSelected = { customer ->
//                            navController.navigate(Destinations.ProductSelection.route + "/${customer.customerId}")
//                        },
//                        onNewCustomerClicked = {
//                            // Handle new customer logic
//                        },
//                        navController = navController,
//                        viewModel = viewModel
//                    )
//                }
//
//                // Product Selection Screen
//                composable(Destinations.ProductSelection.route + "/{customerId}") { backStackEntry ->
//                    val customerId =
//                        backStackEntry.arguments?.getString("customerId")?.toLong()
//                            ?: return@composable
//                    val customer = viewModel.getCustomerById(customerId)
//
//                    if (customer == null) {
//                        // Handle the case where the customer is not found
//                        Text("Customer not found")
//                    } else {
//                        val productList by viewModel.productList.collectAsState()
//
//                        ProductSelectionScreen(
//                            customer = customer,
//                            onProceedToCart = {
//                                // Navigate to the Cart screen with the customer ID
//                                navController.navigate(Destinations.Cart.route + "/${customer.customerId}")
//                            },
//                            navController = navController,
//                            viewModel = viewModel
//                        )
//                    }
//                }
//
//                // Cart Screen
//                composable(Destinations.Cart.route + "/{customerId}") { backStackEntry ->
//                    val customerId =
//                        backStackEntry.arguments?.getString("customerId")?.toLong()
//                            ?: return@composable
//                    val customer = viewModel.getCustomerById(customerId)
//
//                    LaunchedEffect(customerId) {
//                        viewModel.refreshCartItems(customerId)
//                    }
//
//                    if (customer != null) {
//                        CartScreen(
//                            customer = customer,
//                            navController,
//                            viewModel
//                        )
//                    }
//                }
//
//                composable(Destinations.Login.route) {
//                    LoginScreen(viewModel = viewModel, navController)
//                }
//
//                composable(Destinations.LoadStocks.route) {
//                    LoadStockScreen(viewModel = viewModel, navController) {}
//                }
//
//                composable(Destinations.Reports.route) {
//                    ReportsScreen(
//                        viewModel = viewModel,
//                        onInvoiceClick = { invoiceLine ->
//                            navController.navigate(
//                                Destinations.InvoiceDetail.createRoute(
//                                    invoiceLine.invoiceID
//                                )
//                            )
//                        }
//                    )
//                }
//
//                composable(Destinations.InvoiceDetail.route) { backStackEntry ->
//                    val invoiceId = backStackEntry.arguments?.getLong("invoiceId")
//                    invoiceId?.let {
//                        InvoiceDetailScreen(invoiceId = it, viewModel = viewModel)
//                    }
//                }
//
//                composable(Destinations.Order.route) {
//                    OrderScreen(onSubmit = { productName, quantity, dueDate, notes ->
//                        viewModel.submitOrder(productName, quantity, dueDate, notes)
//                        navController.popBackStack()
//                    })
//                }
//
//                composable(Destinations.OrderReports.route) {
//                    OrdersReportsScreen(viewModel = viewModel) {
//                        navController.navigate(Destinations.Order.route)
//                    }
//                }
//            }
//        }
//    }
//}
//

//working logic for authentication


//class AuthViewModel : ViewModel() {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//    private val _authState = MutableLiveData<AuthState>()
//    val authState: LiveData<AuthState> = _authState //    exposing it to ui
//
//    init {
//        checkAuthStatus()
//    }
//
//    //    check user status
//    fun checkAuthStatus() {
//        if (auth.currentUser == null) {
//            _authState.value = AuthState.UnAuthenticated
//        } else {
//            _authState.value = AuthState.Authenticated
//        }
//    }
//
//    //    login method
//    fun login(email: String, password: String) {
//        if (email.isEmpty() || password.isEmpty()) {
//            _authState.value =
//                AuthState.Error("Email or password can't be empty")
//        }
//
//        _authState.value = AuthState.Loading
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    _authState.value = AuthState.Authenticated
//                } else {
//                    _authState.value =
//                        AuthState.Error(task.exception?.message ?: "Something went wrong")
//                }
//
//            }
//    }
//
//    //    signup method
//    fun signup(email: String, password: String) {
//        if (email.isEmpty() || password.isEmpty()) {
//            _authState.value =
//                AuthState.Error("Email or password can't be empty")
//        }
//
//        _authState.value = AuthState.Loading
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    _authState.value = AuthState.Authenticated
//                } else {
//                    _authState.value =
//                        AuthState.Error(task.exception?.message ?: "Something went wrong")
//                }
//
//            }
//    }
//
//    //    signout method
//    fun signout() {
//        auth.signOut()
//        _authState.value = AuthState.UnAuthenticated
//    }
//
//}
// end of auth 00


// end of auth 1

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

//@Composable
//fun InvoiceDetailScreen(
//    invoiceId: Long,
//    viewModel: InvoiceViewModel = viewModel(),
//) {
//
//    // Trigger fetching invoiceLines based on the invoiceId
//    LaunchedEffect(key1 = invoiceId) {
//        viewModel.fetchAllInvoiceLines()
//        viewModel.fetchInvoiceLinesByInvoiceId(invoiceId)
//    }
//    // Collect the filtered invoiceLines
//    val invoiceLines by viewModel.invoiceLines.collectAsState()
//    val invoiceLine = viewModel.getInvoiceLineById(invoiceId)
//
//    val context = LocalContext.current
//    // Build the order details string
//    val linesDetails = invoiceLines.joinToString("\n") { invoiceLine ->
//        "${invoiceLine.productName},     Qty: ${invoiceLine.productQtySold},     $${invoiceLine.productPrice}\n${
//            Date(
//                invoiceLine.invoiceDate
//            )
//        }"
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        Card(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(8.dp),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//
//            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                CustomHeadingMedium(
//                    heading = "INVOICE RECEIPT",
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                if (invoiceLine != null) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp)
//                    ) {
//                        Text(
//                            modifier = Modifier.weight(1f),
//                            text = invoiceLine.customerName,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                        Text(
//                            modifier = Modifier.weight(1f),
//                            text = "${invoiceLine.customerPhone}",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                        Text(
//                            text = "#${invoiceLine.invoiceId}",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                    Text(
//                        modifier = Modifier.padding(8.dp),
//                        text = "${Date(invoiceLine.invoiceDate)}", // Formatting date can be customized
//                        style = MaterialTheme.typography.bodySmall,
//                        textAlign = TextAlign.End
//                    )
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//
//                if (invoiceLines.isNotEmpty()) {
//                    // Display the invoice lines using LazyColumn
//                    LazyColumn(
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(invoiceLines) { invoiceLine ->
//                            TheInvoiceCard(invoiceLine = invoiceLine)
//                        }
//                    }
//                } else {
//                    Text(text = "No items found for this invoice.")
//                }
//            }
//        }
//
//        // Floating Action Button
//        FloatingActionButton(
//            onClick = {
//                // Code for sharing the invoice message...
//
//                val message = "*${Date(System.currentTimeMillis())} REPORT:*\n\n\n" +
//                        "*INVOICE RECEIPT: ${invoiceLine?.invoiceId}*\n" +
//                        "$linesDetails\n\n" +
//                        "THANK YOU!!!... powered by istartk \u00A92024"
//
//                val sendIntent = Intent(Intent.ACTION_SEND).apply {
//                    type = "text/plain"
//                    putExtra(Intent.EXTRA_TEXT, message)
//                }
//
//                try {
//                    val chooserIntent = Intent.createChooser(
//                        sendIntent,
//                        "Send via WhatsApp or WhatsApp Business"
//                    )
//                    chooserIntent.resolveActivity(context.packageManager)?.let {
//                        context.startActivity(chooserIntent)
//                    } ?: run {
//                        Toast.makeText(
//                            context,
//                            "No messaging app found",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                    }
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp) // Add padding from edges
//        ) {
//            Icon(
//                imageVector = Icons.Default.Send,
//                contentDescription = "Send Report",
//                tint = GreenColor
//            )
//        }
//    }
//}
