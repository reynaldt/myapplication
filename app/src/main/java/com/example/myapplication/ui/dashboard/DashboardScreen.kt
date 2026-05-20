package com.example.myapplication.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.ui.components.RequireRole
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onExportCsv: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()
    val sessionManager: SessionManager = koinInject()
    val currentUser by sessionManager.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    RequireRole({ it.canExportReports() }) {
                        IconButton(onClick = onExportCsv) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome banner
            currentUser?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                text = "Welcome, ${user.displayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = user.role.label,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // ── Summary stat cards ────────────────────────────────────────────
            Text(
                text = "Inventory Overview",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total",
                    value = stats.totalItems,
                    icon = Icons.Default.Inventory,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Available",
                    value = stats.availableItems,
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color(0xFF1B5E20).copy(alpha = 0.15f),
                    contentColor = Color(0xFF2E7D32)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Checked Out",
                    value = stats.checkedOutItems,
                    icon = Icons.Default.Output,
                    containerColor = Color(0xFFE65100).copy(alpha = 0.15f),
                    contentColor = Color(0xFFBF360C)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Lost",
                    value = stats.lostItems,
                    icon = Icons.Default.ReportProblem,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Inbound",
                    value = stats.inboundCount,
                    icon = Icons.Default.Input,
                    containerColor = Color(0xFF0D47A1).copy(alpha = 0.12f),
                    contentColor = Color(0xFF1565C0)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Outbound",
                    value = stats.outboundCount,
                    icon = Icons.Default.Output,
                    containerColor = Color(0xFF4A148C).copy(alpha = 0.12f),
                    contentColor = Color(0xFF6A1B9A)
                )
            }

            // ── Category breakdown ────────────────────────────────────────────
            Text(
                text = "Category Breakdown",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val total = stats.totalItems.coerceAtLeast(1)
                    ItemCategory.entries.forEach { cat ->
                        val count = stats.categoryBreakdown[cat] ?: 0
                        CategoryProgressRow(
                            label = cat.label,
                            code = cat.code,
                            count = count,
                            fraction = count.toFloat() / total.toFloat()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Text(
                text = value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = contentColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryProgressRow(
    label: String,
    code: String,
    count: Int,
    fraction: Float
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 800),
        label = "cat_progress_$code"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "[$code] $label",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = count.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
