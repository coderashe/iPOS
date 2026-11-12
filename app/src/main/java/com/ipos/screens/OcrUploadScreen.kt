package com.ipos.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.ipos.data.domain.model.SalesLine
import com.ipos.utils.OcrProcessor
import com.ipos.utils.SalesLineParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrUploadScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var extractedText by remember { mutableStateOf("") }
    var parsedSalesLines by remember { mutableStateOf<List<SalesLine>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        errorMessage = null
        extractedText = ""
        parsedSalesLines = emptyList()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OCR Invoice Upload") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upload Button
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select Image from Gallery")
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Image Preview
            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Process Button
                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            errorMessage = null
                            
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                
                                val text = withContext(Dispatchers.Default) {
                                    val processor = OcrProcessor()
                                    val result = processor.processImage(bitmap)
                                    processor.close()
                                    result
                                }
                                
                                extractedText = text
                                
                                val parser = SalesLineParser()
                                parsedSalesLines = parser.parseOcrText(text)
                                
                            } catch (e: Exception) {
                                errorMessage = e.message
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text("Process Image")
                    }
                }
            }
            
            // Error Message
            errorMessage?.let { error ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Extracted Text
            if (extractedText.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Extracted Text:", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(extractedText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Parsed Sales Lines
            if (parsedSalesLines.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Parsed Sales Lines (${parsedSalesLines.size}):",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        parsedSalesLines.forEach { line ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (line.isValid)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${line.productName} | ${line.size} | ${line.quantity} | \$${line.price}")
                                    if (!line.isValid) {
                                        Text(
                                            line.errorMessage ?: "Invalid",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                // TODO: Navigate to preview/edit screen
                                // Pass parsedSalesLines to next screen
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = parsedSalesLines.any { it.isValid }
                        ) {
                            Text("Continue to Edit & Process")
                        }
                    }
                }
            }
        }
    }
}
