package com.example.learncompose303.app

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.learncompose303.R
import com.example.learncompose303.ui.theme.Green1
import com.example.learncompose303.ui.theme.MyGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

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
                .fillMaxSize()
                .background(MyGray),
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
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = taglineText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.align(Alignment.Start),
                    colors = ButtonDefaults.buttonColors(Green1)
                ) {
                    Text(text = buttonText, color = Color.White)
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
