package com.example.learncompose303

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.screens.ShopLandingScreen
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.screens.SignUpPage
import com.example.learncompose303.ui.theme.LearnCompose303Theme
import com.example.learncompose303.viewmodels.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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

