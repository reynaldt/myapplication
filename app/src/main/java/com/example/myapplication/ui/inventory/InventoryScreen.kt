package com.example.myapplication.ui.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.PrimaryTextLabel
import com.example.myapplication.ui.components.RequireRole
import com.example.myapplication.ui.logs.LogViewModel
import org.koin.androidx.compose.koinViewModel

private enum class InventoryTab { MAIN, INBOUND, CHECKOUT, LOGS, OUTBOUND }

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = koinViewModel(),
    onLogout: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(InventoryTab.MAIN) }

    if (currentTab != InventoryTab.MAIN) {
        BackHandler { currentTab = InventoryTab.MAIN }
    }

    when (currentTab) {
        InventoryTab.MAIN -> InventoryMainScreen(
            onAddInboundClick = { currentTab = InventoryTab.INBOUND },
            onCheckoutClick = { currentTab = InventoryTab.CHECKOUT },
            onViewLogsClick = { currentTab = InventoryTab.LOGS },
            onOutboundClick = { currentTab = InventoryTab.OUTBOUND },
            onLogout = onLogout
        )
        InventoryTab.INBOUND -> AddInboundScreen(viewModel = viewModel, onBack = { currentTab = InventoryTab.MAIN })
        InventoryTab.CHECKOUT -> CheckoutScreen(viewModel = viewModel, onBack = { currentTab = InventoryTab.MAIN })
        InventoryTab.LOGS -> LogsScreen(onBack = { currentTab = InventoryTab.MAIN })
        InventoryTab.OUTBOUND -> OutboundListScreen(viewModel = viewModel, onBack = { currentTab = InventoryTab.MAIN })
    }
}

@Composable
private fun InventoryMainScreen(
    onAddInboundClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onOutboundClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryTextLabel(text = "Actions")

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Check-in: Admin + Staff only
            RequireRole({ it.canCheckIn() }) {
                ActionButton(
                    onClick = onAddInboundClick,
                    icon = Icons.Default.Add,
                    text = "Add Inbound Item"
                )
            }

            // Checkout: Admin + Staff only
            RequireRole({ it.canCheckOut() }) {
                ActionButton(
                    onClick = onCheckoutClick,
                    icon = Icons.Default.Output,
                    text = "Checkout Item"
                )
            }

            ActionButton(
                onClick = onOutboundClick,
                icon = Icons.Default.Inventory,
                text = "Outbound Records"
            )

            ActionButton(
                onClick = onViewLogsClick,
                icon = Icons.Default.History,
                text = "Audit Log"
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
private fun ActionButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(text)
        }
    }
}
