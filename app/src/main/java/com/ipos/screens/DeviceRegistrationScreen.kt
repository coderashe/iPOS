package com.ipos.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ipos.screens.auth.AuthViewModel
import com.ipos.viewmodels.DeviceRegistrationViewModel
import com.ipos.viewmodels.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRegistrationScreen(
    onRegistrationComplete: () -> Unit,
    viewModel: DeviceRegistrationViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    setupViewModel: SetupViewModel = hiltViewModel()
) {
    val deviceManager = viewModel.deviceManager
    var deviceName by remember { mutableStateOf(deviceManager.getDeviceName()) }
    var storeIdInput by remember { mutableStateOf(viewModel.deviceSession.storeId) }
    var isLoading by remember { mutableStateOf(false) }
    var backfillDone by remember { mutableStateOf(false) }
    val deviceId = deviceManager.getDeviceId()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Setup") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.Devices,
                contentDescription = "Device",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Device Setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Give this device a friendly name to identify it in reports and analytics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = deviceId.take(16) + "...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Store",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Store ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = storeIdInput.ifBlank { "Not set" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = storeIdInput,
                onValueChange = { storeIdInput = it.uppercase() },
                label = { Text("Store ID") },
                placeholder = { Text("e.g., ISTART, HANDP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Device Name") },
                placeholder = { Text("e.g., Main Counter, Mobile POS 1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Text(
                text = "Suggestions: Main Counter, Checkout 1, Mobile POS, Back Office",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Backfill button — stamps DeviceId & StoreId on all legacy records missing them
            OutlinedButton(
                onClick = {
                    backfillDone = false
                    setupViewModel.runBackfill()
                    backfillDone = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Text(if (backfillDone) "✓ Records Updated" else "Backfill Device & Store IDs")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    isLoading = true
                    viewModel.registerDevice(
                        deviceName = deviceName,
                        storeId = storeIdInput.trim(),
                        onComplete = onRegistrationComplete
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = deviceName.isNotBlank() && storeIdInput.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Update Device Name")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
