package com.example.myapplication.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: InventoryViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val inventoryList by viewModel.inventoryList.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Only show items that are inbound + available
    val checkoutCandidates = inventoryList.filter {
        it.movement == "inbound" && it.status == ItemStatus.AVAILABLE.name
    }

    var selectedItemForCheckout by remember { mutableStateOf<InventoryEntity?>(null) }
    var selectedItemForDetails by remember { mutableStateOf<InventoryEntity?>(null) }
    var picName by remember { mutableStateOf("") }
    var checkoutNotes by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showCheckoutDialog, currentUser?.id) {
        if (showCheckoutDialog) {
            picName = currentUser?.displayName.orEmpty()
        }
    }

    if (selectedItemForDetails != null) {
        InventoryItemDetailDialog(
            item = selectedItemForDetails!!,
            onDismiss = { selectedItemForDetails = null }
        )
    }

    if (showCheckoutDialog && selectedItemForCheckout != null) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false; picName = ""; checkoutNotes = "" },
            title = { Text("Checkout Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Checking out: ${selectedItemForCheckout?.itemName}")
                    Text(
                        text = "Code: ${selectedItemForCheckout?.inventoryCode}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = picName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Released to (Logged-in Profile)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    OutlinedTextField(
                        value = checkoutNotes,
                        onValueChange = { checkoutNotes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedItemForCheckout?.id?.let { id ->
                            viewModel.checkoutItem(id, picName, checkoutNotes)
                        }
                        showCheckoutDialog = false; picName = ""; checkoutNotes = ""
                    },
                    enabled = currentUser != null
                ) { Text("Confirm Checkout") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false; picName = ""; checkoutNotes = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show success/error snackbar feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(operationState) {
        when (val s = operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetOperationState()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetOperationState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Inventory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (operationState is OperationState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (checkoutCandidates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                        Text("No available items to checkout", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(checkoutCandidates, key = { it.id }) { item ->
                        CheckoutItemCard(
                            item = item,
                            onCardClick = { selectedItemForDetails = item },
                            onCheckoutClick = { selectedItemForCheckout = item; showCheckoutDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutItemCard(
    item: InventoryEntity,
    onCardClick: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.picture.isNullOrBlank() && File(item.picture).exists()) {
                AsyncImage(
                    model = File(item.picture),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.inventoryCode, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("[${ItemCategory.fromString(item.category).label}]  Qty: ${item.quantity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            Button(
                onClick = onCheckoutClick,
                shape = RoundedCornerShape(8.dp)
            ) { Text("Checkout") }
        }
    }
}
