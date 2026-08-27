package com.example.learncompose303.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import com.example.learncompose303.viewmodels.InvoiceViewModel
import kotlinx.coroutines.delay

@Composable
fun SyncStatusCard(
    viewModel: InvoiceViewModel = viewModel()
) {
    val unsyncedCount by viewModel.notsyncedCount.collectAsState()
    val workState    by viewModel.syncWorkState.collectAsState()

    val isRunning = workState == WorkInfo.State.RUNNING
    val succeeded = workState == WorkInfo.State.SUCCEEDED
    val failed    = workState == WorkInfo.State.FAILED ||
                    workState == WorkInfo.State.CANCELLED

    // Always allow tapping — let the worker decide if there's anything to sync
    val canClick = !isRunning

    // Show "Sync Complete!" for 3 s after a real SUCCESS then let unsyncedCount drive the display
    var showComplete by remember { mutableStateOf(false) }
    LaunchedEffect(succeeded) {
        if (succeeded) {
            showComplete = true
            delay(3000)
            showComplete = false
        }
    }

    val cardColor by animateColorAsState(
        targetValue = when {
            showComplete         -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            failed               -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            unsyncedCount > 0    -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            else                 -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(500)
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            showComplete      -> MaterialTheme.colorScheme.primary
            failed            -> MaterialTheme.colorScheme.error
            unsyncedCount > 0 -> MaterialTheme.colorScheme.error
            else              -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(500)
    )

    // Continuous rotation while running
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                if (canClick) {
                    viewModel.syncInvoiceLines()
                }
            },
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        showComplete      -> "Sync Complete!"
                        isRunning         -> "Syncing..."
                        failed            -> "Sync Failed — tap to retry"
                        unsyncedCount > 0 -> "Unsynced Invoice Lines"
                        else              -> "All Synced"
                    },
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = iconColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        showComplete      -> "All invoice lines are now synced"
                        isRunning         -> "Please wait..."
                        failed            -> "Check connection and try again"
                        unsyncedCount > 0 -> "$unsyncedCount items need syncing"
                        else              -> "Everything is up to date"
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
                    showComplete -> Icon(
                        imageVector     = Icons.Default.CheckCircle,
                        contentDescription = "Synced",
                        tint            = iconColor,
                        modifier        = Modifier.size(32.dp)
                    )
                    isRunning -> Icon(
                        imageVector     = Icons.Default.CloudSync,
                        contentDescription = "Syncing",
                        tint            = iconColor,
                        modifier        = Modifier
                            .size(32.dp)
                            .rotate(rotation)   // real continuous spin while worker runs
                    )
                    unsyncedCount > 0 -> Box {
                        Icon(
                            imageVector     = Icons.Default.Warning,
                            contentDescription = "Needs Sync",
                            tint            = iconColor,
                            modifier        = Modifier.size(32.dp)
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
                                text  = unsyncedCount.toString(),
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> Icon(
                        imageVector     = Icons.Default.CheckCircle,
                        contentDescription = "All Synced",
                        tint            = iconColor,
                        modifier        = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
