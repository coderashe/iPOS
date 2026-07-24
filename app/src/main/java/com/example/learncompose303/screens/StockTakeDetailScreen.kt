package com.example.learncompose303.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.learncompose303.app.utils.StockTakePdfGenerator
import com.example.learncompose303.data.entities.StockTakeSessionStatus
import com.example.learncompose303.viewmodels.StockTakeDetail
import com.example.learncompose303.viewmodels.StockTakeDetailState
import com.example.learncompose303.viewmodels.StockTakeViewModel
import com.example.learncompose303.viewmodels.SuggestionType
import com.example.learncompose303.viewmodels.VarianceRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTakeDetailScreen(
    sessionId: Long,
    stockTakeViewModel: StockTakeViewModel = hiltViewModel(),
    navController: NavController
) {
    val detailState by stockTakeViewModel.detailState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sessionId) {
        stockTakeViewModel.loadSessionDetail(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (detailState is StockTakeDetailState.Ready) {
                ExtendedFloatingActionButton(
                    onClick = {
                        StockTakePdfGenerator.generateAndShare(
                            context,
                            (detailState as StockTakeDetailState.Ready).detail
                        )
                    },
                    icon = {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Download PDF")
                    },
                    text = { Text("Export PDF") }
                )
            }
        }
    ) { padding ->
        when (val state = detailState) {
            is StockTakeDetailState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is StockTakeDetailState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is StockTakeDetailState.Ready -> {
                DetailContent(
                    detail = state.detail,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// ─── Main content ─────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(detail: StockTakeDetail, modifier: Modifier = Modifier) {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header card ───────────────────────────────────────────────────
        item {
            SessionHeaderCard(detail = detail, fmt = fmt)
        }

        // ── KPI row ───────────────────────────────────────────────────────
        item {
            SectionTitle("Overview")
            Spacer(Modifier.height(8.dp))
            KpiGrid(detail = detail)
        }

        // ── Suggestions ───────────────────────────────────────────────────
        if (detail.suggestions.isNotEmpty()) {
            item { SectionTitle("Business Insights") }
            items(detail.suggestions) { suggestion ->
                SuggestionCard(suggestion = suggestion)
            }
        }

        // ── Variance table ────────────────────────────────────────────────
        if (detail.varianceRows.isNotEmpty()) {
            item {
                SectionTitle("Variances  (${detail.varianceRows.size})")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sorted by magnitude — largest discrepancy first",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { VarianceTableHeader() }
            itemsIndexed(detail.varianceRows) { index, row ->
                VarianceRowItem(row = row)
            }
        }

        // ── Uncounted gap analysis ────────────────────────────────────────
        if (detail.uncountedProducts.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                SectionTitle("Not Counted  (${detail.uncountedProducts.size})")
                Spacer(Modifier.height(4.dp))
                Text(
                    "These products were in the catalog but not recorded in this session.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            items(detail.uncountedProducts, key = { it.product.productId }) { pwb ->
                Text(
                    "• ${pwb.brand.brandName} - ${pwb.product.productDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)
                )
            }
        }

        // Bottom FAB spacing
        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ─── Session header ───────────────────────────────────────────────────────────

@Composable
private fun SessionHeaderCard(
    detail: StockTakeDetail,
    fmt: SimpleDateFormat
) {
    val status = StockTakeSessionStatus.valueOf(detail.session.status)
    val (statusColor, statusLabel) = when (status) {
        StockTakeSessionStatus.OPEN -> Color(0xFF1976D2) to "In Progress"
        StockTakeSessionStatus.COMPLETED -> Color(0xFF388E3C) to "Completed"
        StockTakeSessionStatus.CANCELLED -> Color(0xFF757575) to "Cancelled"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Session #${detail.session.sessionId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LabelValueDetail("Started by", detail.session.startedBy)
                LabelValueDetail(
                    "Started",
                    fmt.format(Date(detail.session.startedAt))
                )
            }
            if (detail.session.closedAt != null) {
                Spacer(Modifier.height(6.dp))
                LabelValueDetail("Closed", fmt.format(Date(detail.session.closedAt)))
            }
        }
    }
}

// ─── KPI grid ─────────────────────────────────────────────────────────────────

private data class KpiItem(val label: String, val value: String, val color: Color)

@Composable
private fun KpiGrid(detail: StockTakeDetail) {
    val kpis = listOf(
        KpiItem("Counted", "${detail.countedCount}", Color(0xFF1565C0)),
        KpiItem("Total Catalog", "${detail.totalCatalog}", Color(0xFF37474F)),
        KpiItem("Coverage", "${detail.coveragePct}%",
            if (detail.coveragePct >= 80) Color(0xFF2E7D32)
            else if (detail.coveragePct >= 50) Color(0xFFF57F17)
            else Color(0xFFC62828)
        ),
        KpiItem("Variances", "${detail.varianceCount}", Color(0xFFF57F17)),
        KpiItem("Shortages", "${detail.shortageCount}", Color(0xFFC62828)),
        KpiItem("Surpluses", "${detail.surplusCount}", Color(0xFF2E7D32))
    )

    // 3-column grid
    kpis.chunked(3).forEach { row ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { kpi ->
                KpiCard(kpi = kpi, modifier = Modifier.weight(1f))
            }
            // Fill empty slots if row has < 3 items
            repeat(3 - row.size) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun KpiCard(kpi: KpiItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = kpi.color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                kpi.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = kpi.color,
                fontSize = 20.sp
            )
            Text(
                kpi.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Suggestion card ──────────────────────────────────────────────────────────

@Composable
private fun SuggestionCard(
    suggestion: com.example.learncompose303.viewmodels.StockTakeSuggestion
) {
    val (bgColor, iconColor, icon) = when (suggestion.type) {
        SuggestionType.WARNING -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFF57F17),
            Icons.Default.Warning
        )
        SuggestionType.OK -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        SuggestionType.INFO -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1565C0),
            Icons.Default.Info
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    suggestion.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    suggestion.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── Variance table ───────────────────────────────────────────────────────────

@Composable
private fun VarianceTableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Product",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(3f)
        )
        Text(
            "Sys",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            "Counted",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            "Var",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VarianceRowItem(row: VarianceRow) {
    val varColor = if (row.variance < 0) Color(0xFFC62828) else Color(0xFF2E7D32)
    val varText = if (row.variance < 0) "%.0f".format(row.variance)
    else "+%.0f".format(row.variance)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                row.displayName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(3f)
            )
            Text(
                row.systemQty.toString(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                "%.0f".format(row.countedQty),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                varText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = varColor,
                modifier = Modifier.weight(1f)
            )
        }
        if (!row.reason.isNullOrBlank()) {
            Text(
                "Reason: ${row.reason}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
            )
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun LabelValueDetail(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}


