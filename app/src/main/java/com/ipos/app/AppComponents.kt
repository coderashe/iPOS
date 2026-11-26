package com.ipos.app

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.ipos.R
import com.ipos.ui.theme.GreenColor
import com.ipos.ui.theme.MyGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun DisplayCurrentDate() {
    Text(text = getCurrentTimestamp())
}

@Composable
fun DisplayTotalBalance(modifier: Modifier, balance: String) {
    Text(
        text = balance,
        color = Color.Black,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun DisplayArrowRight(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.KeyboardArrowRight,
        contentDescription = "",
        modifier.size(22.dp),
        tint = Color.Black
    )
}

@Composable
fun DisplayArrowLeft(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.KeyboardArrowLeft,
        contentDescription = "",
        modifier.size(22.dp),
        tint = Color.Black
    )
}

@Composable
fun CustomHeadingMedium(heading: String) {
    Text(
        text = heading,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 12.dp),
//        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ThreeItemRow(
    modifier: Modifier = Modifier,
    startContent: @Composable () -> Unit,
    centerContent: @Composable () -> Unit,
    endContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Aligns the three components at start, center, and end
    ) {
        // Start content (e.g. left arrow, icon, etc.)
        startContent()

        // Center content (e.g. text, image, etc.)
        centerContent()

        // End content (e.g. right arrow, button, etc.)
        endContent()
    }
}

@Composable
fun TwoItemColumn(
    modifier: Modifier = Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
) {

    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        topContent()
        bottomContent()
    }

}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            Color(0xFFF0F0F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            Color.White,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun SocialSignInButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp),
        tint = backgroundColor,
    )
    Spacer(modifier = Modifier.width(8.dp))
//    Text(text = text, fontSize = 16.sp)

}


//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun Carousel(
//    modifier: Modifier = Modifier
//        .padding(12.dp)
//) {
//    val pagerState = rememberPagerState(pageCount = { 3 })
//
//    HorizontalPager(
//        state = pagerState,
//        modifier = Modifier.fillMaxWidth()
//    ) { page ->
//        when (page) {
//            0 -> CarouselCard(
//                headingText = "First Card",
//                taglineText = "This is the first tagline.",
//                buttonText = "Explore",
//                imageResource = painterResource(id = R.drawable.meat3),
//                onButtonClick = { /* TODO: Handle click */ }
//            )
//
//            1 -> CarouselCard(
//                headingText = "Second Card",
//                taglineText = "This is the second tagline.",
//                buttonText = "Discover",
//                imageResource = painterResource(id = R.drawable.chicken1),
//                onButtonClick = { /* TODO: Handle click */ }
//            )
//
//            2 -> CarouselCard(
//                headingText = "Third Card",
//                taglineText = "This is the third tagline.",
//                buttonText = "Learn More",
//                imageResource = painterResource(id = R.drawable.meat7),
//                onButtonClick = { /* TODO: Handle click */ }
//            )
//        }
//    }
//}

/**
 * Two-step dialog for creating a full shoe brand with 9 products (sizes 3–11).
 *
 * Step 1: Brand name — resolved via case-insensitive lookup, created if new.
 * Step 2: Product description + avg cost + std cost + counted qty per size.
 *
 * [onConfirm] receives: brandName, description, avgCost, stdCost, countedQtyPerSize.
 * The caller is responsible for delegating to the appropriate ViewModel function.
 */
@Composable
fun AddBrandWithProductsDialog(
    initialBrandName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (
        brandName: String,
        description: String,
        averageCost: Double,
        standardCost: Double,
        countedQtyPerSize: Double
    ) -> Unit
) {
    var step by rememberSaveable { mutableStateOf(1) }

    // Step 1
    var brandName by rememberSaveable { mutableStateOf(initialBrandName) }

    // Step 2
    var description by rememberSaveable { mutableStateOf("") }
    var averageCostText by rememberSaveable { mutableStateOf("") }
    var standardCostText by rememberSaveable { mutableStateOf("") }
    var countedQtyText by rememberSaveable { mutableStateOf("100") }

    val descriptionFocusRequester = remember { FocusRequester() }

    val step1Valid = brandName.isNotBlank()
    val step2Valid = description.isNotBlank() &&
            averageCostText.toDoubleOrNull() != null &&
            standardCostText.toDoubleOrNull() != null &&
            countedQtyText.toDoubleOrNull() != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Header
                Text(
                    text = "Add New Brand & Products",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Creates 9 products (sizes 3–11) with 100 units each.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Step indicator
                AddBrandStepIndicator(currentStep = step, totalSteps = 2)

                Spacer(Modifier.height(20.dp))

                if (step == 1) {
                    Text(
                        "Step 1: Brand Name",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("Brand Name *") },
                        placeholder = { Text("e.g. Nike, Adidas, Puma") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { if (step1Valid) step = 2 }
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "If this brand already exists, it will be reused.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Step 2: Product Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Brand: ${brandName.trim()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    LaunchedEffect(Unit) {
                        descriptionFocusRequester.requestFocus()
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description *") },
                        placeholder = { Text("e.g. Air Force 1") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(descriptionFocusRequester),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = averageCostText,
                            onValueChange = { averageCostText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Avg Cost *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = standardCostText,
                            onValueChange = { standardCostText = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Std Cost *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = countedQtyText,
                        onValueChange = { countedQtyText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Qty per Size *") },
                        placeholder = { Text("Default: 100") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (step2Valid) {
                                    onConfirm(
                                        brandName.trim(),
                                        description.trim(),
                                        averageCostText.toDouble(),
                                        standardCostText.toDouble(),
                                        countedQtyText.toDouble()
                                    )
                                }
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        supportingText = {
                            Text("9 products (sizes 3–11) will be created.")
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Action buttons
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        if (step == 1) onDismiss() else step = 1
                    }) {
                        Text(if (step == 1) "Cancel" else "Back")
                    }

                    if (step == 1) {
                        Button(
                            onClick = { step = 2 },
                            enabled = step1Valid
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                onConfirm(
                                    brandName.trim(),
                                    description.trim(),
                                    averageCostText.toDoubleOrNull() ?: 0.0,
                                    standardCostText.toDoubleOrNull() ?: 0.0,
                                    countedQtyText.toDoubleOrNull() ?: 100.0
                                )
                            },
                            enabled = step2Valid
                        ) {
                            Text("Save & Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddBrandStepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep

            val dotColor = when {
                isActive || isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }

            Box(
                modifier = Modifier
                    .size(if (isActive) 28.dp else 24.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive || isCompleted)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < totalSteps - 1) {
                Divider(
                    modifier = Modifier.width(32.dp),
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

/**
 * Clean dialog for creating a single product for non-shoe businesses (Grocery, Butcher, General).
 * Used from ProductSelectionScreen and StockTakingScreen when a product is not found in search.
 */
@Composable
fun AddSingleProductDialog(
    initialName: String = "",
    businessType: com.ipos.app.core.BusinessType? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        productName: String,
        category: String,
        unit: String,
        sellingPrice: Double,
        unitCost: Double,
        stockQty: Double
    ) -> Unit
) {
    val categories = when (businessType) {
        com.ipos.app.core.BusinessType.BUTCHER -> listOf("Beef", "Chicken", "Pork", "Lamb", "Fish", "Other")
        else -> listOf("Beverages", "Dairy", "Bakery", "Produce", "Canned Goods", "Snacks", "Household", "General")
    }
    val units = when (businessType) {
        com.ipos.app.core.BusinessType.BUTCHER -> listOf("kg", "weight", "count", "pack")
        else -> listOf("item", "pack", "kg", "g", "litre", "bottle", "can", "box")
    }

    var productName by rememberSaveable { mutableStateOf(initialName) }
    var selectedCategory by rememberSaveable { mutableStateOf(categories.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedUnit by rememberSaveable { mutableStateOf(units.first()) }
    var unitExpanded by remember { mutableStateOf(false) }
    var sellingPriceText by rememberSaveable { mutableStateOf("") }
    var unitCostText by rememberSaveable { mutableStateOf("") }
    var stockQtyText by rememberSaveable { mutableStateOf("100") }

    val isValid = productName.isNotBlank() &&
            sellingPriceText.toDoubleOrNull() != null &&
            unitCostText.toDoubleOrNull() != null &&
            stockQtyText.toDoubleOrNull() != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = "Add New Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                val subtitle = when (businessType) {
                    com.ipos.app.core.BusinessType.GROCERY -> "Add a grocery item to catalog"
                    com.ipos.app.core.BusinessType.BUTCHER -> "Add a butcher item to catalog"
                    else -> "Add product to catalog"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Product Name
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Tea, Milk, Rice") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(10.dp))

                // Category & Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            label = { Text("Category") },
                            readOnly = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { categoryExpanded = !categoryExpanded }
                                )
                            }
                        )
                        androidx.compose.material.DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                androidx.compose.material.DropdownMenuItem(onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }) { Text(cat) }
                            }
                        }
                    }

                    // Unit
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            label = { Text("Unit") },
                            readOnly = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { unitExpanded = !unitExpanded }
                                )
                            }
                        )
                        androidx.compose.material.DropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { u ->
                                androidx.compose.material.DropdownMenuItem(onClick = {
                                    selectedUnit = u
                                    unitExpanded = false
                                }) { Text(u) }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Selling Price & Cost Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Selling Price *") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { unitCostText = it },
                        label = { Text("Unit Cost *") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Initial Stock Qty
                OutlinedTextField(
                    value = stockQtyText,
                    onValueChange = { stockQtyText = it },
                    label = { Text("Initial Stock Qty *") },
                    placeholder = { Text("100") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val sp = sellingPriceText.toDoubleOrNull() ?: 0.0
                            val uc = unitCostText.toDoubleOrNull() ?: 0.0
                            val qty = stockQtyText.toDoubleOrNull() ?: 100.0
                            onConfirm(productName.trim(), selectedCategory, selectedUnit, sp, uc, qty)
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save & Add")
                    }
                }
            }
        }
    }
}
