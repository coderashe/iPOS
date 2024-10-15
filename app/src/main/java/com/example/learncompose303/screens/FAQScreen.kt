package com.example.learncompose303.screens

import androidx.lifecycle.viewmodel.compose.viewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Instrumentation
import android.content.IntentSender
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.learncompose303.R
import com.example.learncompose303.data.Destinations
import com.example.learncompose303.data.entities.Product
import com.example.learncompose303.screens.auth.AuthViewModel
import com.example.learncompose303.ui.theme.Blue1
import com.example.learncompose303.ui.theme.Gray1
import com.example.learncompose303.ui.theme.Green1
import com.example.learncompose303.ui.theme.LightGray
import com.example.learncompose303.ui.theme.LightGreen
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.viewmodels.InvoiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.Calendar

@Composable
fun FAQScreen(
    modifier: Modifier = Modifier
) {
    val faqs = listOf(
        "What is iPOS?" to "iPOS is a mobile Point of Sale system that helps businesses manage inventory, create invoices, and track sales efficiently.",
        "How do I add new products to the iPOS app?" to "Go to the Load Stocks screen, select 'Add New Product,' and enter the necessary details such as product name, price, and stock level.",
        "Can I generate sales reports with iPOS?" to "Yes, the iPOS app allows you to generate daily, weekly, and monthly sales reports directly from the Reports screen.",
        "Is my data backed up automatically?" to "Yes, the iPOS app uses Firebase to automatically back up your sales, product, and invoice data to the cloud.",
        "Can I manage multiple users on iPOS?" to "Yes, iPOS supports multiple user roles such as Admin, Cashier, and Manager. You can manage user permissions in the Settings screen."
    )

    var expandedFaqIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Frequently Asked Questions",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF4CAF50), // Green heading color
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Loop through the FAQs list
        faqs.forEachIndexed { index, faq ->
            FAQCard(
                question = faq.first,
                answer = faq.second,
                isExpanded = expandedFaqIndex == index,
                onCardClick = {
                    expandedFaqIndex = if (expandedFaqIndex == index) -1 else index
                }
            )
            Spacer(modifier = Modifier.height(8.dp)) // Spacing between cards
        }
    }
}

@Composable
fun FAQCard(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .animateContentSize(), // Smooth animation for expanding and collapsing
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "FAQ Icon",
                    tint = Color(0xFF4CAF50), // Green color for the icon
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Left
                )
            }
        }
    }
}
