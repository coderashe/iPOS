package com.example.learncompose303.app

import androidx.lifecycle.viewmodel.compose.viewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Instrumentation
import android.content.IntentSender
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import com.example.learncompose303.R
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
fun CarouselCard(
    headingText: String,
    taglineText: String,
    buttonText: String,
    imageResource: Painter, // Use painter for images
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
//        elevation = 8.dp, Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column for text content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = headingText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = taglineText,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Text(text = buttonText)
                }
            }

            // Column for image content
            Image(
                painter = imageResource,
                contentDescription = "Carousel Image",
                modifier = Modifier
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoSlidingCarousel(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope() // Coroutine scope to launch coroutines

    val pagerState = rememberPagerState(pageCount = { 3 })

    // Auto-slide functionality with LaunchedEffect and coroutine delay
    LaunchedEffect(pagerState) {
        while (true) {
            yield() // Yield ensures smooth continuation
            delay(5000L) // Delay of 5 seconds for auto slide
            val nextPage = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(
                    durationMillis = 600, // Adjust the duration for smoother animation
                    easing = FastOutSlowInEasing // Easing function for smooth acceleration/deceleration
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> CarouselCard(
                    headingText = "First Card",
                    taglineText = "This is the first tagline.",
                    buttonText = "Explore",
                    imageResource = painterResource(id = R.drawable.meat3),
                    onButtonClick = { /* TODO: Handle click */ }
                )

                1 -> CarouselCard(
                    headingText = "Second Card",
                    taglineText = "This is the second tagline.",
                    buttonText = "Discover",
                    imageResource = painterResource(id = R.drawable.chicken1),
                    onButtonClick = { /* TODO: Handle click */ }
                )

                2 -> CarouselCard(
                    headingText = "Third Card",
                    taglineText = "This is the third tagline.",
                    buttonText = "Learn More",
                    imageResource = painterResource(id = R.drawable.meat7),
                    onButtonClick = { /* TODO: Handle click */ }
                )
            }
        }

        // Indicator dots below the carousel
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (pagerState.currentPage == index) Green1 else Color.Gray,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(4.dp)
                        .clickable {
                            // Use coroutineScope to launch the scrollToPage call
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SalesmanInfoCard(
    salesmanName: String,
    shopName: String,
    viewModel: InvoiceViewModel = viewModel()
) {
    val (currentSalesTotal, numberOfTransactions) = viewModel.getTodaySalesInfo()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name of Salesman
            Text(
                text = salesmanName,
                style = MaterialTheme.typography.titleMedium,
            )

            // The Shop
            Text(
                text = "@$shopName",
                style = MaterialTheme.typography.bodySmall,
                color = Gray1
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Current sales total
                Column {
                    Text(
                        text = "SALES TOTAL",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "$${String.format("%.2f", currentSalesTotal)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightGreen
                    )
                }

                // Number of transactions
                Column {
                    Text(
                        text = "no.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "$numberOfTransactions",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightGreen
                    )
                }
            }
        }
    }
}

@Composable
fun SpacerHeight() {
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DueDatePicker(dueDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // State to track the selected due date
    var selectedDate by remember { mutableStateOf(dueDate) }

    // Date Picker Dialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = "$dayOfMonth/${month + 1}/$year"
                onDateSelected(selectedDate) // Notify parent about the selected date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Due Date Input (with Calendar Picker)
    OutlinedTextField(
        value = selectedDate,
        onValueChange = { }, // No manual changes allowed for read-only input
        label = { Text("Due Date") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }, // Show the date picker dialog on click
        readOnly = true, // Prevent typing, make it clickable only
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Pick Date"
            )
        }
    )
}

@Composable
fun Title() {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.onBackground,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onPrimary.copy(0.5f)
    )
    var offsetX by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = tween(3000)
        ) { value, _ ->
            offsetX = value
        }
    }
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(offsetX, 0f),
        end = Offset(offsetX + 200f, 100f)
    )
    Text(
        stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.titleLarge.copy(brush = brush)
    )
}