package com.example.learncompose303.data.domain.util

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