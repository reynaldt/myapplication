package com.example.myapplication.ui.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.UserRole
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
    var currentTabName by rememberSaveable { mutableStateOf(InventoryTab.MAIN.name) }
    val currentTab = InventoryTab.valueOf(currentTabName)

    if (currentTab != InventoryTab.MAIN) {
        BackHandler { currentTabName = InventoryTab.MAIN.name }
    }

    when (currentTab) {
        InventoryTab.MAIN -> InventoryMainScreen(
            onAddInboundClick = { currentTabName = InventoryTab.INBOUND.name },
            onCheckoutClick = { currentTabName = InventoryTab.CHECKOUT.name },
            onViewLogsClick = { currentTabName = InventoryTab.LOGS.name },
            onOutboundClick = { currentTabName = InventoryTab.OUTBOUND.name },
            onLogout = onLogout
        )
        InventoryTab.INBOUND -> AddInboundScreen(viewModel = viewModel, onBack = { currentTabName = InventoryTab.MAIN.name })
        InventoryTab.CHECKOUT -> CheckoutScreen(viewModel = viewModel, onBack = { currentTabName = InventoryTab.MAIN.name })
        InventoryTab.LOGS -> LogsScreen(onBack = { currentTabName = InventoryTab.MAIN.name })
        InventoryTab.OUTBOUND -> OutboundListScreen(viewModel = viewModel, onBack = { currentTabName = InventoryTab.MAIN.name })
    }
}

@Composable
internal fun InventoryMainScreen(
    onAddInboundClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onOutboundClick: () -> Unit,
    onLogout: () -> Unit,
    role: UserRole? = null
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
            RequireRole({ it.canCheckIn() }, role = role) {
                ActionButton(
                    onClick = onAddInboundClick,
                    icon = Icons.Default.Add,
                    text = "Add Inbound Item",
                    modifier = Modifier.testTag("InventoryActionAddInbound")
                )
            }

            // Checkout: Admin + Staff only
            RequireRole({ it.canCheckOut() }, role = role) {
                ActionButton(
                    onClick = onCheckoutClick,
                    icon = Icons.Default.Output,
                    text = "Checkout Item",
                    modifier = Modifier.testTag("InventoryActionCheckout")
                )
            }

            ActionButton(
                onClick = onOutboundClick,
                icon = Icons.Default.Inventory,
                text = "Outbound Records",
                modifier = Modifier.testTag("InventoryActionOutbound")
            )

            ActionButton(
                onClick = onViewLogsClick,
                icon = Icons.Default.History,
                text = "Audit Log",
                modifier = Modifier.testTag("InventoryActionLogs")
            )

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
private fun ActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
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
