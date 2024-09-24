package com.example.learncompose303.app

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.R
import com.example.learncompose303.data.City
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.YoutubeData
import com.example.learncompose303.data.getYoutubeVideos
import com.example.learncompose303.screens.HomeScreen
import com.example.learncompose303.screens.MyIncomeScreen
import com.example.learncompose303.screens.NotificationsScreen
import com.example.learncompose303.screens.PostScreen
import com.example.learncompose303.screens.PriorityScreen
import com.example.learncompose303.screens.ProfileScreen
import com.example.learncompose303.screens.RideRequestsScreen
import com.example.learncompose303.screens.SearchScreen
import com.example.learncompose303.screens.TTTScreen
import com.example.learncompose303.screens.WalletScreen
import com.example.learncompose303.screens.YouTubeUI
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ButtonComponents() {

    val context = LocalContext.current.applicationContext

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(Color.Gray)
        ) {
            Text(text = "Login")
        }
    }

}

@Composable
fun DisplayYoutubeData() {
    val videoList = listOf(
        YoutubeData(
            R.drawable.google,
            stringResource(id = R.string.videoTitle1),
            stringResource(id = R.string.videoChannel1)
        ),
        YoutubeData(
            R.drawable.facebook,
            stringResource(id = R.string.videoTitle2),
            stringResource(id = R.string.videoChannel2)
        ),
        YoutubeData(
            R.drawable.loc1,
            stringResource(id = R.string.videoTitle3),
            stringResource(id = R.string.videoChannel3)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(videoList) { video ->
            YouTubeUI(youtubeData = video)

        }
    }
}

@Composable
fun LearnCheckBoxes() {
    val todoList = listOf(
        "Goto Gym",
        "Goto Shchool",
        "Goto Work",
        "Goto Band Practice",
        "Goto Movie",
        "Goto Bed"
    )

    val context = LocalContext.current.applicationContext

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 80.dp),
    ) {
        todoList.forEach { todoItem ->
            var isChecked by remember {
                mutableStateOf(false)
            }

            Row {
                Checkbox(checked = isChecked, onCheckedChange = { checkedStatus ->
                    isChecked = checkedStatus
                    Toast.makeText(context, "$todoItem is $isChecked", Toast.LENGTH_SHORT).show()
                })
                Text(
                    text = todoItem,
                    fontSize = 20.sp
                )
            }
        }

    }

}


@Composable
fun LearnFAB() {

    val context = LocalContext.current.applicationContext

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = GreenColor,
            onClick = {
                Toast.makeText(context, "FAB clicked", Toast.LENGTH_SHORT).show()
            }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        }

    }
}

@Composable
fun LearnExtendedFAB() {

    val context = LocalContext.current.applicationContext

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ExtendedFloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = GreenColor,
            onClick = {
                Toast.makeText(context, "ExtendedFAB clicked", Toast.LENGTH_SHORT).show()
            }) {
            Icon(imageVector = Icons.Filled.Menu, contentDescription = null)
            Text(
                text = "MENU",
                modifier = Modifier.padding(4.dp)
            )
        }

    }
}

@Composable
fun LearnState() {
//    var age = 0

    var age by remember {
        mutableStateOf(0)
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = { age++ }) {
            Text(text = "I am $age years old")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnTopAppBar() {

    val context = LocalContext.current.applicationContext

    TopAppBar(
        title = {
            Text(text = "Google")
        },
        navigationIcon = {
            IconButton(onClick = {
                Toast.makeText(context, "Google ", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "google"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GreenColor,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = {
                Toast.makeText(context, R.string.profile, Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(id = R.string.profile),
                    tint = Color.White
                )
            }
            IconButton(onClick = {
                Toast.makeText(context, R.string.search, Toast.LENGTH_SHORT).show()
            }) {

                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.search),
                    tint = Color.White
                )
            }
            IconButton(onClick = {
                Toast.makeText(context, R.string.menu, Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.menu),
                    tint = Color.White
                )
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnBottomNav() {
    val navigationController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val selected = remember {
        mutableStateOf(Icons.Default.Home)
    }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = GreenColor
            ) {
                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Home
                        navigationController.navigate(
                            Destinations.Home.route
                        ) {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = if (selected.value == Icons.Default.Home) Color.White else Color.DarkGray
                    )
                }

//                for Search icon

                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Search
                        navigationController.navigate(
                            Destinations.Search.route
                        ) {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = if (selected.value == Icons.Default.Search) Color.White else Color.DarkGray
                    )
                }

//

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(onClick = {
                        showBottomSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GreenColor
                        )

                    }
                }
//

                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Notifications
                        navigationController.navigate(
                            Destinations.Notifications.route
                        ) {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = if (selected.value == Icons.Default.Notifications) Color.White else Color.DarkGray
                    )
                }
//

                IconButton(
                    onClick = {
                        selected.value = Icons.Default.Person
                        navigationController.navigate(
                            Destinations.Profile.route
                        ) {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = if (selected.value == Icons.Default.Person) Color.White else Color.DarkGray
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navigationController,
            startDestination = Destinations.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Destinations.Home.route) { HomeScreen() }
            composable(Destinations.Search.route) { SearchScreen() }
            composable(Destinations.Notifications.route) { NotificationsScreen() }
            composable(Destinations.Profile.route) { ProfileScreen() }
            composable(Destinations.Post.route) { PostScreen() }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BottomSheetItem(icon = Icons.Default.ThumbUp, title = "Create a Post") {
                    showBottomSheet = false
                    navigationController.popBackStack(Destinations.Post.route, inclusive = false)
                }

                BottomSheetItem(icon = Icons.Default.Star, title = "Add a Story") {
                    Toast.makeText(context, "Add a Story", Toast.LENGTH_SHORT).show()
                }

                BottomSheetItem(icon = Icons.Default.PlayArrow, title = "Add a Reel") {
                    Toast.makeText(context, "Add a Reel", Toast.LENGTH_SHORT).show()
                }

                BottomSheetItem(icon = Icons.Default.Favorite, title = "Go Live") {
                    Toast.makeText(context, "Go Live", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }
}

@Composable
fun BottomSheetItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = GreenColor)
        Text(text = title, fontSize = 22.sp, color = GreenColor)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnnavDrawer() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current.applicationContext

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenColor)
                        .height(150.dp)
                ) {
                    Text(text = "")
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = "Home", color = GreenColor) },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "home",
                            tint = GreenColor
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navController.navigate(Destinations.Home.route) {
                            popUpTo(0)
                        }
                    })

//
                NavigationDrawerItem(
                    label = { Text(text = "Profile", color = GreenColor) },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "home",
                            tint = GreenColor
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navController.navigate(Destinations.Profile.route) {
                            popUpTo(0)
                        }
                    })
//

                NavigationDrawerItem(
                    label = { Text(text = "Notifications", color = GreenColor) },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "notifications",
                            tint = GreenColor
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navController.navigate(Destinations.Notifications.route) {
                            popUpTo(0)
                        }
                    })
//

                NavigationDrawerItem(
                    label = { Text(text = "Logout", color = GreenColor) },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "logout",
                            tint = GreenColor
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                    })

//
            }
        }) {
        Scaffold(
            topBar = {
                val coroutineScope = rememberCoroutineScope()
                TopAppBar(
                    title = { Text(text = "Facebook") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GreenColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "MenuButton"
                            )
                        }
                    }
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Destinations.Home.route
            ) {
                composable(Destinations.Home.route) { HomeScreen() }
                composable(Destinations.Notifications.route) { NotificationsScreen() }
                composable(Destinations.Profile.route) { ProfileScreen() }
            }
        }

    }

}

@Composable
fun InstagramProfile() {
    val profileImage = painterResource(id = R.drawable.google)
    val img2 = painterResource(id = R.drawable.loc1)
    val img3 = painterResource(id = R.drawable.facebook)
    val img4 = painterResource(id = R.drawable.google)
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(150.dp)
                .padding(8.dp)
        ) {
//            1st column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = profileImage,
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            }
//            end 1st column

            //            2nd column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "120",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = stringResource(id = R.string.posts),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                )
            }
//            end 2nd column

            //            3rd column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "3721",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = stringResource(id = R.string.followers),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                )
            }
//            end 3rd column

            //            4th column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "1223",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = stringResource(id = R.string.followers),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                )
            }
//            end 4th column

        }
//        end of 1st row

//        user profile
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.username),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = R.string.profile_description),
                fontWeight = FontWeight.Normal,
            )
        }
//        end of user profile

//        profile Button

        Row(modifier = Modifier.padding(8.dp)) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(Color.LightGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.profile_button),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        //        end of profile Button

//        Posts
        Column(modifier = Modifier.background(Color.Black)) {
            Row(modifier = Modifier.fillMaxWidth(1f)) {
                Image(
                    painter = img4,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 1.dp, bottom = 1.dp)
                        .aspectRatio(1f),
                )
                Image(
                    painter = img3,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 1.dp, bottom = 1.dp)
                        .aspectRatio(1f),
                )
                Image(
                    painter = img2,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                )
            }

//            end of row 1

            Row(modifier = Modifier.fillMaxWidth(1f)) {
                Image(
                    painter = img4,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 1.dp, bottom = 1.dp)
                        .aspectRatio(1f),
                )
                Image(
                    painter = img3,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 1.dp, bottom = 1.dp)
                        .aspectRatio(1f),
                )
                Image(
                    painter = img2,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                )
            }
//            end of row 2
        }
//        end of Posts
    }
}

@Composable
fun TestLazyLists() {
    val names = listOf(
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc",
        "Abc"
    )
//    LazyColumn() {
//        items(names) {
//            Text(text = "Product: $it")
//            Divider()
//        }
//    }

    val images = listOf(
        R.drawable.google,
        R.drawable.facebook,
        R.drawable.loc1
    )

    val gridImages = arrayListOf<Int>()
    for (i in 0..100) {
        gridImages.add(images.random())
    }

    LazyVerticalGrid(GridCells.Adaptive(100.dp)) {
        items(gridImages) {
            val painterRes = painterResource(id = it)

            Image(
                painter = painterRes, contentDescription = null,
                modifier = Modifier.aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun YotubeVideoList() {
    LazyColumn {
        item {
            Text(
                text = "Trending Videos",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                modifier = Modifier.padding(4.dp)
            )
        }

        items(getYoutubeVideos()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MyGray)
            ) {
                val painter = painterResource(id = it.thumbnail)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(2f)
                ) {
                    Text(text = it.videoTitle, fontWeight = FontWeight.Bold)
                    Text(text = it.channelName, fontSize = 10.sp)
                }

            }
        }
    }
}

fun makeToast(ctx: Context, msg: String) {
    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}

@Composable
fun TestFAB() {
    val context = LocalContext.current.applicationContext
    val courotineScope = rememberCoroutineScope()

    FloatingActionButton(onClick = {
//        makeToast(context, "Clicked")
        courotineScope.launch {

        }
    }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LearnScaffold() {
    Scaffold(
        floatingActionButton = { TestFAB() }
    ) {
        YotubeVideoList()
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CitiesList(
    list: List<City>,
    onAddCity: (name: String, country: String) -> Unit,
    onSort: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val showDialog = remember {
        mutableStateOf(false)
    }

    Scaffold(
        floatingActionButton = { AddCityFAB { showDialog.value = true } },
        topBar = { AddTopBar(onSort) }
    ) {
        if (list.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No cities available")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp)
            ) {
                items(list) {
                    Column(
                        modifier = Modifier
                            .padding(5.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = MyGray)
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = it.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = it.country)
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        AddCityDialog(
            onDismiss = { showDialog.value = false },
            onConfirm = { name, country ->
                showDialog.value = false
                onAddCity.invoke(name, country)
            }
        )
    }
}

@Composable
fun AddCityFAB(onFABClicked: () -> Unit) {
    FloatingActionButton(onClick = onFABClicked) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityDialog(onDismiss: () -> Unit, onConfirm: (name: String, country: String) -> Unit) {

    val cityName = remember { mutableStateOf(TextFieldValue("")) }
    val countryName = remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD A CITY",
                modifier = Modifier.padding(5.dp),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = cityName.value,
                    onValueChange = { cityName.value = it },
                    label = { Text(text = "City Name") },
                    placeholder = { Text(text = "Harare") },
                    modifier = Modifier.padding(5.dp)
                )
                TextField(
                    value = countryName.value,
                    onValueChange = { countryName.value = it },
                    label = { Text(text = "Country") },
                    placeholder = { Text(text = "Zimbabwe") },
                    modifier = Modifier.padding(5.dp)
                )
            }
        },

        confirmButton = {
            Button(onClick = {
                val n = cityName.value.text
                val c = countryName.value.text

                if (n.isNotEmpty() && c.isNotEmpty())
                    onConfirm.invoke(n, c)


            }) {
                Text(text = "Add")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss }) {
                Text(text = "Cancel")
            }
        }
    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTopBar(onSort: () -> Unit) {
    val myIcon = painterResource(id = R.drawable.sort)

    TopAppBar(
        title = { Text(text = "Visited Cities") },
        actions = {
            IconButton(onClick = onSort) {
                Icon(painter = myIcon, contentDescription = null)
            }
        }
    )

}

fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun DisplayCurrentDate() {
    Text(text = getCurrentTimestamp())
}

@Composable
fun DisplayTotalBalance(modifier: Modifier) {
    Text(
        text = stringResource(id = R.string.cash),
        color = Color.Black,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun DisplayArrowRight(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.KeyboardArrowRight,
        contentDescription = "",
        modifier.size(22.dp),
        tint = Color.Black
    )
}

@Composable
fun DisplayArrowLeft(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.KeyboardArrowLeft,
        contentDescription = "",
        modifier.size(22.dp),
        tint = Color.Black
    )
}

@Composable
fun CustomHeadingMedium(heading: String) {
    Text(
        text = heading,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 8.dp),
//        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ThreeItemRow(
    modifier: Modifier = Modifier,
    startContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Aligns the three components at start, center, and end
    ) {
        // Start content (e.g. left arrow, icon, etc.)
        startContent()

        // Center content (e.g. text, image, etc.)
        centerContent()

        // End content (e.g. right arrow, button, etc.)
        endContent()
    }
}

@Composable
fun TwoItemColumn(
    modifier: Modifier = Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
) {

    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        topContent()
        bottomContent()
    }

}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            Color(0xFFF0F0F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            Color.White,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun SocialSignInButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp),
        tint = backgroundColor,
    )
    Spacer(modifier = Modifier.width(8.dp))
//    Text(text = text, fontSize = 16.sp)

}


//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun Carousel(
//    modifier: Modifier = Modifier
//        .padding(12.dp)
//) {
//    val pagerState = rememberPagerState(pageCount = { 3 })
//
//    HorizontalPager(
//        state = pagerState,
//        modifier = Modifier.fillMaxWidth()
//    ) { page ->
//        when (page) {
//            0 -> CarouselCard(
//                headingText = "First Card",
//                taglineText = "This is the first tagline.",
//                buttonText = "Explore",
//                imageResource = painterResource(id = R.drawable.meat3),
//                onButtonClick = { /* TODO: Handle click */ }
//            )
//
//            1 -> CarouselCard(
//                headingText = "Second Card",
//                taglineText = "This is the second tagline.",
//                buttonText = "Discover",
//                imageResource = painterResource(id = R.drawable.chicken1),
//                onButtonClick = { /* TODO: Handle click */ }
//            )
//
//            2 -> CarouselCard(
//                headingText = "Third Card",
//                taglineText = "This is the third tagline.",
//                buttonText = "Learn More",
//                imageResource = painterResource(id = R.drawable.meat7),
//                onButtonClick = { /* TODO: Handle click */ }
//            )
//        }
//    }
//}

