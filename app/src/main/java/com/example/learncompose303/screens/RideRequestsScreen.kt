package com.example.learncompose303.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.example.learncompose303.R

@Composable
fun RideRequestsScreen() {
    val searchImage = painterResource(id = R.drawable.searching)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.rides),
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(8.dp)
            )
            Image(
                painter = searchImage, contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit // Crop and scale the image to the center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWidthToggleSwitch() {
    var isChecked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(120.dp), // Set the width to 120.dp
        contentAlignment = Alignment.Center
    ) {
        // Switch component with custom width
        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            modifier = Modifier
                .fillMaxWidth() // Ensure switch takes full width
                .width(200.dp)
        )

        // Text overlay
        Text(
            text = if (isChecked) "ON" else "OFF",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomWidthToggleSwitch() {
    CustomWidthToggleSwitch()
}