package com.example.learncompose303

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.app.ButtonComponents
import com.example.learncompose303.app.DefaultScreen
import com.example.learncompose303.app.InDriveTopBottomNavigation
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.screens.CustomerSelectionScreen
import com.example.learncompose303.screens.FacebookScaffold
import com.example.learncompose303.screens.HomeScreen
import com.example.learncompose303.screens.ItemListScreen
import com.example.learncompose303.screens.LoginScreen
import com.example.learncompose303.screens.RideRequestsScreen
import com.example.learncompose303.screens.SelectImage
import com.example.learncompose303.screens.ShopLandingScreen
import com.example.learncompose303.ui.theme.LearnCompose303Theme
import com.example.learncompose303.viewmodels.HomeViewModel
import com.example.learncompose303.viewmodels.InvoiceViewModel
import com.example.learncompose303.viewmodels.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearnCompose303Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {

                    ShopLandingScreen()

                }
            }
        }
    }
}
