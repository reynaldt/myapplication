package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.local.entity.InventoryEntity
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.ItemStatus
import com.example.myapplication.ui.components.InventoryItemDetailDialog
import com.example.myapplication.ui.inventory.InventoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: InventoryViewModel = koinViewModel()
) {
    val inventoryList by viewModel.inventoryList.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var selectedItem by remember { mutableStateOf<InventoryEntity?>(null) }

    if (selectedItem != null) {
        InventoryItemDetailDialog(
            item = selectedItem!!,
            onDismiss = { selectedItem = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventory") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = filter.query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or code…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Category filter chips ─────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filter.category == null,
                        onClick = { viewModel.updateCategoryFilter(null) },
                        label = { Text("All", fontSize = 12.sp) }
                    )
                }
                items(ItemCategory.entries) { cat ->
                    FilterChip(
                        selected = filter.category == cat,
                        onClick = { viewModel.updateCategoryFilter(if (filter.category == cat) null else cat) },
                        label = { Text(cat.label, fontSize = 12.sp) }
                    )
                }
            }

            // ── Status filter chips ───────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filter.status == null,
                        onClick = { viewModel.updateStatusFilter(null) },
                        label = { Text("All Status", fontSize = 12.sp) }
                    )
                }
                items(ItemStatus.entries) { st ->
                    FilterChip(
                        selected = filter.status == st,
                        onClick = { viewModel.updateStatusFilter(if (filter.status == st) null else st) },
                        label = { Text(st.label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Inventory list ────────────────────────────────────────────────
            if (inventoryList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Text(
                            text = if (filter.query.isNotEmpty() || filter.category != null || filter.status != null)
                                "No items match your filter"
                            else "No inventory items yet",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(inventoryList, key = { it.id }) { item ->
                        InventoryItemCard(item) { selectedItem = item }
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(item: InventoryEntity, onClick: () -> Unit) {
    val statusColor = when (item.status) {
        ItemStatus.AVAILABLE.name   -> MaterialTheme.colorScheme.primary
        ItemStatus.CHECKED_OUT.name -> MaterialTheme.colorScheme.error
        ItemStatus.LOST.name        -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    val statusLabel = ItemStatus.fromString(item.status).label

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (!item.picture.isNullOrBlank() && File(item.picture).exists()) {
                AsyncImage(
                    model = File(item.picture),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.itemName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = item.inventoryCode,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "[${ItemCategory.fromString(item.category).label}]  Qty: ${item.quantity}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (!item.itemDescription.isNullOrBlank()) {
                    Text(
                        text = item.itemDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
