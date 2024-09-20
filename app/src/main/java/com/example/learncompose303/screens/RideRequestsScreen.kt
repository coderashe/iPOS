package com.example.learncompose303.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learncompose303.R
import com.example.learncompose303.app.AutoSlidingCarousel
import com.example.learncompose303.app.CustomHeadingMedium
import com.example.learncompose303.app.SalesmanInfoCard
import com.example.learncompose303.app.SpacerHeight
import com.example.learncompose303.data.Destinations

@Composable
fun RideRequestsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(top = 4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SalesmanInfoCard(
            salesmanName = "Simba Start",
            shopName = "ISTART CAPITAL MRT",
            currentSalesTotal = 12456.78,
            numberOfTransactions = 52
        )
        SpacerHeight()
        SearchBarExample()
        CustomHeadingMedium(
            heading = stringResource(R.string.popular_products)
        )
        AutoSlidingCarousel()
        CustomHeadingMedium(
            heading = stringResource(R.string.more)
        )
        QuickAccessButtons(navController)
    }

}

