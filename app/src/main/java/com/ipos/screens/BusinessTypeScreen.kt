package com.ipos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.SetMeal
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipos.app.core.BusinessType
import com.ipos.viewmodels.SettingsViewModel

@Composable
fun BusinessTypeScreen(
    onComplete: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var selected by remember { mutableStateOf<BusinessType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to iPOS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "What kind of business are you running?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        BusinessTypeCard(
            type = BusinessType.SHOES,
            icon = { Icon(Icons.Default.Checkroom, contentDescription = null, modifier = Modifier.size(40.dp)) },
            description = "Track stock by brand & size (3–11)",
            selected = selected == BusinessType.SHOES,
            onClick = { selected = BusinessType.SHOES }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BusinessTypeCard(
            type = BusinessType.GROCERY,
            icon = { Icon(Icons.Default.ShoppingBasket, contentDescription = null, modifier = Modifier.size(40.dp)) },
            description = "Products by category — Food, Drinks, Sweets, etc.",
            selected = selected == BusinessType.GROCERY,
            onClick = { selected = BusinessType.GROCERY }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BusinessTypeCard(
            type = BusinessType.BUTCHER,
            icon = { Icon(Icons.Default.SetMeal, contentDescription = null, modifier = Modifier.size(40.dp)) },
            description = "Sell by weight (kg) or item count",
            selected = selected == BusinessType.BUTCHER,
            onClick = { selected = BusinessType.BUTCHER }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                selected?.let {
                    viewModel.saveBusinessType(it)
                    onComplete()
                }
            },
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (selected != null) "Get Started with ${selected!!.displayName}" else "Select a business type",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This cannot be changed later.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun BusinessTypeCard(
    type: BusinessType,
    icon: @Composable () -> Unit,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
