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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learncompose303.R
import com.example.learncompose303.ui.theme.GreenColor
import com.example.learncompose303.ui.theme.MyBlueish
import com.example.learncompose303.ui.theme.MyGray
import com.example.learncompose303.ui.theme.TheGrey

//@Composable
//fun WalletScreen() {
//
//    var isChecked by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MyGray)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(8.dp)
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
////            1st box for topup :: , \n row2 (total wallet balance & >), \n row3 (topupButton)
//
//            Column(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(24.dp))
//                    .background(Color.White)
//            ) {
////               row1 (icon Balance & ?)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.White)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.ThumbUp,
//                        contentDescription = null,
////                        tint = Color(0xff66cc00),
//                        tint = MyBlueish,
//                        modifier = Modifier
//                            .size(40.dp)
//                            .padding(2.dp)
//                            .clip(CircleShape)
//                    )
//                    Text(
//                        text = stringResource(id = R.string.balance),
//                        color = Color.Black,
//                        fontSize = 16.sp,
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(4.dp)
//                    )
//                    Icon(
//                        imageVector = Icons.Default.Refresh,
//                        contentDescription = null,
//                        tint = TheGrey,
//                        modifier = Modifier
//                            .padding(8.dp)
//                    )
//
//                }
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.White)
//                ) {
//
//                    Text(
//                        text = stringResource(id = R.string.cash),
//                        color = Color.Black,
//                        fontSize = 30.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier
//                            .weight(1f)
//                    )
//
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = "",
//                        modifier = Modifier
//                            .padding(end = 8.dp),
//                        tint = TheGrey
//                    )
//                }
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.White)
//                ) {
//                    Button(
//                        onClick = { /* Handle button click */ },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(2.dp)),
//                        colors = ButtonDefaults.buttonColors(MyGray) // Lime Green color
//                    ) {
//                        Text(
//                            text = "Top up",
//                            color = Color.Black,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
////            2nd panel
//
//            Column(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(18.dp))
//                    .background(Color.White)
//            ) {
////               row1 (icon Balance & ?)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.White)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Star,
//                        contentDescription = null,
//                        tint = TheGrey,
//                        modifier = Modifier
//                            .size(36.dp)
//                            .padding(2.dp)
//                            .clip(CircleShape)
//                    )
//
//                    Column(
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .weight(1f)
//                    ) {
//                        Text(
//                            text = stringResource(id = R.string.couriers),
//                            color = Color.Black,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            text = stringResource(id = R.string.showinrequest),
//                            color = Color.Black
//                        )
//                    }
//
//                    Box(
//                        contentAlignment = Alignment.Center
//                    ) {
//                        // Switch component with custom width
//                        Switch(
//                            checked = isChecked,
//                            onCheckedChange = { isChecked = it },
//                        )
//                    }
//
//                }
//
//            }
//        }
//    }
//}

@Composable
fun WalletScreen() {
    var isChecked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MyGray)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reusable BalanceCard component
            BalanceCard(
                balanceLabel = stringResource(id = R.string.balance),
                balanceAmount = stringResource(id = R.string.cash),
                onTopUpClick = { /* Handle Top Up */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reusable SwitchRow component
            SwitchRow(
                icon = Icons.Default.Star,
                title = stringResource(id = R.string.couriers),
                subtitle = stringResource(id = R.string.showinrequest),
                isChecked = isChecked,
                onCheckedChange = { isChecked = it }
            )
        }
    }
}

@Composable
fun BalanceCard(
    balanceLabel: String,
    balanceAmount: String,
    onTopUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        // Reusable IconRow component for the balance label and icon
        IconRow(
            icon = Icons.Default.ThumbUp,
            label = balanceLabel,
            trailingIcon = Icons.Default.Refresh
        )

        // Balance amount row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        ) {
            Text(
                text = balanceAmount,
                color = Color.Black,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "",
                tint = TheGrey,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Top-up button
        ActionButton(
            text = "Top up",
            onClick = onTopUpClick
        )
    }
}

@Composable
fun IconRow(
    icon: ImageVector,
    label: String,
    trailingIcon: ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MyBlueish,
            modifier = Modifier
                .size(40.dp)
                .padding(2.dp)
                .clip(CircleShape)
        )
        Text(
            text = label,
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
        )
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = TheGrey,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp)),
        colors = ButtonDefaults.buttonColors(MyGray)
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TheGrey,
            modifier = Modifier
                .size(36.dp)
                .padding(2.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier
                .padding(8.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.Black
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}


@Preview
@Composable
fun WalletPreview() {
    WalletScreen()
}

