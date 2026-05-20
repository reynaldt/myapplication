package com.example.myapplication.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.entity.LogEntity
import com.example.myapplication.ui.logs.LogViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: LogViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val logs by viewModel.allLogs.collectAsState()
    var filterAction by remember { mutableStateOf<String?>(null) }

    val filteredLogs = if (filterAction == null) logs else logs.filter { it.action == filterAction }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            // Action filter chips
            ScrollableTabRow(
                selectedTabIndex = 0,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp,
                indicator = {},
                divider = {}
            ) {
                val actions = listOf(null, "CHECK_IN", "CHECK_OUT", "MARK_LOST", "DELETE")
                val labels = listOf("All", "Check In", "Check Out", "Lost", "Delete")
                actions.forEachIndexed { i, action ->
                    FilterChip(
                        selected = filterAction == action,
                        onClick = { filterAction = if (filterAction == action) null else action },
                        label = { Text(labels[i], fontSize = 12.sp) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.HistoryToggleOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No audit logs yet",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        AuditLogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: LogEntity) {
    val (actionColor, actionLabel, actionIcon) = when (log.action) {
        "CHECK_IN"   -> Triple(Color(0xFF2E7D32), "Check In",   Icons.Default.Input)
        "CHECK_OUT"  -> Triple(Color(0xFFBF360C), "Check Out",  Icons.Default.Output)
        "MARK_LOST"  -> Triple(Color(0xFFC62828), "Mark Lost",  Icons.Default.ReportProblem)
        "DELETE"     -> Triple(Color(0xFF424242), "Delete",     Icons.Default.Delete)
        else         -> Triple(Color(0xFF1565C0), "Update",     Icons.Default.Edit)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = actionColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(actionIcon, contentDescription = null, tint = actionColor, modifier = Modifier.size(14.dp))
                            Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = actionColor)
                        }
                    }
                }
                Text(
                    text = log.inventoryCode,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Item name
            Text(
                text = log.itemName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // User + time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(log.performedByUsername, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(log.timestamp, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            // Notes
            if (!log.notes.isNullOrBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(
                        text = log.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
