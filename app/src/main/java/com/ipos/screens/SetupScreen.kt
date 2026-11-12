package com.ipos.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipos.viewmodels.SetupViewModel
import com.ipos.viewmodels.StepStatus

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    onRegisterDevice: () -> Unit = {},
    viewModel: SetupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    // Kick off setup on first composition
    LaunchedEffect(Unit) {
        viewModel.runSetup(context)
    }

    // Navigate when done
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onSetupComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Setting up your device",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Please keep this screen open until all steps complete.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                // Step rows
                state.steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (step.status) {
                            StepStatus.PENDING -> Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            StepStatus.RUNNING -> CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            StepStatus.DONE -> Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            StepStatus.ERROR -> Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = step.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (step.status == StepStatus.RUNNING)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = when (step.status) {
                                    StepStatus.PENDING -> Color.Gray
                                    StepStatus.RUNNING -> MaterialTheme.colorScheme.primary
                                    StepStatus.DONE -> MaterialTheme.colorScheme.onSurface
                                    StepStatus.ERROR -> MaterialTheme.colorScheme.error
                                }
                            )
                            if (step.status == StepStatus.RUNNING) {
                                Text(
                                    text = "In progress...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Divider()

                // Manual backfill button — fixes legacy records missing DeviceId/StoreId
                OutlinedButton(
                    onClick = { viewModel.runBackfill() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Backfill Device & Store IDs")
                }

                // Error + retry
                if (state.canRetry) {
                    Divider()
                    state.errorMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // If storeId is missing, show device registration CTA
                    if (state.errorMessage?.contains("Store ID") == true) {
                        Button(
                            onClick = { onRegisterDevice() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Register This Device")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.retry(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
