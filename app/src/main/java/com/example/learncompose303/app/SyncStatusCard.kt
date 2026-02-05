package com.example.learncompose303.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learncompose303.ui.theme.MediumGreen
import com.example.learncompose303.viewmodels.InvoiceViewModel
import kotlinx.coroutines.delay

@Composable
fun SyncStatusCard(
    viewModel: InvoiceViewModel = viewModel()
) {
    var isSyncing by remember { mutableStateOf(false) }
    var syncComplete by remember { mutableStateOf(false) }
    val unsyncedCount by viewModel.notsyncedCount.collectAsState()
    
    // Animation values
    val rotationAngle by animateFloatAsState(
        targetValue = if (isSyncing) 360f else 0f,
        animationSpec = tween(1000),
        finishedListener = { 
            if (isSyncing) {
                isSyncing = false
                syncComplete = true
            }
        }
    )
    
    val cardColor by animateColorAsState(
        targetValue = when {
            syncComplete -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            unsyncedCount > 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(500)
    )
    
    val iconColor by animateColorAsState(
        targetValue = when {
            syncComplete -> MaterialTheme.colorScheme.primary
            unsyncedCount > 0 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(500)
    )
    
    // Reset sync complete after delay
    LaunchedEffect(syncComplete) {
        if (syncComplete) {
            delay(3000)
            syncComplete = false
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                if (!isSyncing && unsyncedCount > 0) {
                    isSyncing = true
                    viewModel.syncInvoiceLines()
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        syncComplete -> "Sync Complete!"
                        isSyncing -> "Syncing..."
                        unsyncedCount > 0 -> "Unsynced Invoice Lines"
                        else -> "All Synced"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when {
                        syncComplete -> "All invoice lines are now synced"
                        isSyncing -> "Please wait..."
                        unsyncedCount > 0 -> "$unsyncedCount items need syncing"
                        else -> "Everything is up to date"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconColor.copy(alpha = 0.7f)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    syncComplete -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Synced",
                            tint = iconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    isSyncing -> {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Syncing",
                            tint = iconColor,
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(rotationAngle)
                        )
                    }
                    unsyncedCount > 0 -> {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Needs Sync",
                                tint = iconColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .offset(x = 8.dp, y = (-8).dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unsyncedCount.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "All Synced",
                            tint = iconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}