package com.example.learncompose303.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.R
import com.example.learncompose303.app.DisplayArrowLeft
import com.example.learncompose303.app.DisplayArrowRight
import com.example.learncompose303.app.DisplayCurrentDate
import com.example.learncompose303.app.DisplayTotalBalance
import com.example.learncompose303.app.ThreeItemRow
import com.example.learncompose303.app.TwoItemColumn
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyGray

@Composable
fun MyIncomeScreen() {
    Column(
        modifier = Modifier
            .background(MyGray)
            .verticalScroll(rememberScrollState())  // Makes it scrollable if content is large
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//            1st layer filter by day, week, month

        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
            ) {
                HighlightedButtonRow()
            }
//            display date
            DisplayCurrentDate()

//            ArrowAndBalanceRow
            ArrowAndBalanceRow()

//            no complete orders
            CompleteOrdersSection()

        }
        Spacer(
            modifier = Modifier
                .height(6.dp)
                .fillMaxWidth()
                .background(Color.Gray)
        )

//        last section :: history, achievements
        HistorySection()
        AchievementsSection()

    }
}

@Composable
fun HistorySection() {
    ThreeItemRow(
        modifier = Modifier.padding(12.dp),
        startContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "",
                    modifier = Modifier
                        .size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between the icon and text
                Text(
                    text = stringResource(R.string.goto_request_history),
                    color = Color.Black
                )
            }
        },
        centerContent = {
            // Empty composable since centerContent will be empty in this case
        },
        endContent = {
            DisplayArrowRight(modifier = Modifier)
        }
    )
}

@Composable
fun AchievementsSection() {
    ThreeItemRow(
        modifier = Modifier.padding(12.dp),
        startContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "",
                    modifier = Modifier
                        .size(24.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between the icon and text
                Text(
                    text = stringResource(R.string.achievements),
                    color = Color.Black
                )
            }
        },
        centerContent = {
            // Empty composable since centerContent will be empty in this case
        },
        endContent = {
            DisplayArrowRight(modifier = Modifier)
        }
    )
}

@Composable
fun ArrowAndBalanceRow() {
    ThreeItemRow(
        startContent = {
            DisplayArrowLeft(modifier = Modifier)
        },
        centerContent = {
            DisplayTotalBalance(modifier = Modifier)
        },
        endContent = {
            DisplayArrowRight(modifier = Modifier)
        }
    )
}

@Composable
fun CompleteOrdersSection() {
    TwoItemColumn(
        modifier = Modifier.height(260.dp),
        topContent = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier
                    .size(36.dp),
                tint = Color.Gray
            )
        },
        bottomContent = {
            Text(
                text = stringResource(R.string.no_complete_orders),
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    )
}


@Composable
fun HighlightedButtonRow() {
    // State to track the currently selected button
    var selectedButton by remember { mutableStateOf("Day") }

    Row(
        modifier = Modifier
            .padding(8.dp)
    ) {
        // First Button (Day)
        Button(
            onClick = { selectedButton = "Day" },
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(
                if (selectedButton == "Day") Color(0xFF3B5998) else Color.LightGray, // Pale dark blue for selected
                contentColor = if (selectedButton == "Day") Color.White else Color.Black // White for selected text
            )
        ) {
            Text(
                text = "Day",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Second Button (Week)
        Button(
            onClick = { selectedButton = "Week" },
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(
                if (selectedButton == "Week") Color(0xFF3B5998) else Color.LightGray,
                contentColor = if (selectedButton == "Week") Color.White else Color.Black
            )
        ) {
            Text(
                text = "Week",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Third Button (Month)
        Button(
            onClick = { selectedButton = "Month" },
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp)),
            colors = ButtonDefaults.buttonColors(
                if (selectedButton == "Month") Color(0xFF3B5998) else Color.LightGray,
                contentColor = if (selectedButton == "Month") Color.White else Color.Black
            )
        ) {
            Text(
                text = "Month",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun IncomePrev() {
    MyIncomeScreen()
}