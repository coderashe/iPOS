package com.example.learncompose303.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
