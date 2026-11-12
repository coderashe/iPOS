package com.ipos.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipos.R
import com.ipos.ui.theme.GreenColor
import com.ipos.ui.theme.MyBlueish
import com.ipos.ui.theme.MyGray
import com.ipos.viewmodels.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PriorityScreen(
    viewModel: InvoiceViewModel = viewModel()
) {

    val (currentSalesTotal, numberOfTransactions) = viewModel.getMonthlySalesInfo()

    // Observe the total count of all orders
    val totalOrdersCount by viewModel.orderCount.collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .background(MyGray)
            .verticalScroll(rememberScrollState())  // Makes it scrollable if content is large
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//            1st layer of prirority screen

        Column(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.priority),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MyBlueish
                )
            }

//                    2nd row
            Row(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.low),
                    color = Color.Black,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
//                    3rd row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // First Progress Bar (60% with gradient)
                Column(
                    modifier = Modifier
                        .height(60.dp)
                        .padding(4.dp)
                        .weight(1f)
                ) {
                    LinearProgressIndicator(
                        progress = 0.6f, // 60%
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .height(8.dp)
                            .background(
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = GreenColor // Makes the indicator itself transparent to show the background
                    )

                    Text(
                        text = stringResource(id = R.string.reviewsmedium),
                        color = Color.Black,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .height(60.dp)
                        .padding(4.dp)
                        .weight(1f)
                ) {
                    // Second Progress Bar (30% with red color)
                    LinearProgressIndicator(
                        progress = 0.3f, // 30%
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .height(8.dp)
                            .background(
                                Color.LightGray, // Background of the bar
                                shape = RoundedCornerShape(50)
                            ),
                        color = Color.Red // Solid red color for the bar
                    )
                    Text(
                        text = stringResource(id = R.string.experiencemedium),
                        color = Color.Black,
                    )

                }

            }

//            4th row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(
                    modifier = Modifier
                        .height(60.dp)
                        .padding(4.dp)
                        .weight(1f)
                ) {
                    // Second Progress Bar (30% with red color)
                    LinearProgressIndicator(
                        progress = 0.3f, // 30%
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .height(8.dp)
                            .background(
                                Color.LightGray, // Background of the bar
                                shape = RoundedCornerShape(50)
                            ),
                        color = Color.Red // Solid red color for the bar
                    )
                    Text(
                        text = stringResource(id = R.string.reputation),
                        color = Color.Black,
                    )

                }

                Spacer(modifier = Modifier.width(16.dp))

                // First Progress Bar (60% with gradient)
                Column(
                    modifier = Modifier
                        .height(60.dp)
                        .padding(4.dp)
                        .weight(1f)
                ) {
                    LinearProgressIndicator(
                        progress = 0.6f, // 60%
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .height(8.dp)
                            .background(
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = GreenColor // Makes the indicator itself transparent to show the background
                    )

                    Text(
                        text = stringResource(id = R.string.frequency),
                        color = Color.Black,
                    )
                }


            }

        }

//        layer 2: rides & reviews totals

        Column(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

//                    get totalInvoices
                    Text(
                        text = numberOfTransactions.toString(),
                        color = MyBlueish,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(id = R.string.navinvoices),
                        color = Color.Black,
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = totalOrdersCount.toString(),
                        color = MyBlueish,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(id = R.string.navorders),
                        color = Color.Black,
                    )
                }

            }
        }

//        layer 3 : customer reviews section


        repeat(8) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "1 " + stringResource(id = R.string.revw),
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = getCurrentTimestamp(),
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                }

                val smiley = painterResource(id = R.drawable.smiley)

                Row(
                    modifier = Modifier
                        .padding(4.dp)
                ) {

                    Image(
                        painter = smiley, contentDescription = "smile",
                        modifier = Modifier
                            .size(42.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                    )
                    // Column containing text, star icons, and feedback
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.passenger),
                            color = Color.Black
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star rating",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Yellow
                                )
                            }
                        }
                        Text(
                            text = " Was excellent",
                            color = Color.Black
                        )
                    }

                }
            }
        }


    }
}

fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}


