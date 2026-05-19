package com.example.myapplication.ui.inventory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.PrimaryTextLabel
import org.koin.androidx.compose.koinViewModel

private enum class InventoryTab {
    MAIN,
    INBOUND,
    CHECKOUT,
    LOGS,
    OUTBOUND
}

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = koinViewModel()
) {
    var currentTab by remember { mutableStateOf(InventoryTab.MAIN) }

    // Intercept physical/system back presses if the user is inside a sub-screen
    if (currentTab != InventoryTab.MAIN) {
        BackHandler {
            currentTab = InventoryTab.MAIN
        }
    }

    when (currentTab) {
        InventoryTab.MAIN -> {
            InventoryMainScreen(
                onAddInboundClick = { currentTab = InventoryTab.INBOUND },
                onCheckoutClick = { currentTab = InventoryTab.CHECKOUT },
                onViewLogsClick = { currentTab = InventoryTab.LOGS },
                onOutboundClick = { currentTab = InventoryTab.OUTBOUND }
            )
        }

        InventoryTab.INBOUND -> {
            AddInboundScreen(
                viewModel = viewModel,
                onBack = { currentTab = InventoryTab.MAIN }
            )
        }

        InventoryTab.CHECKOUT -> {
            CheckoutScreen(
                viewModel = viewModel,
                onBack = { currentTab = InventoryTab.MAIN }
            )
        }
        
        InventoryTab.LOGS -> {
            LogsScreen(
                viewModel = viewModel,
                onBack = { currentTab = InventoryTab.MAIN }
            )
        }

        InventoryTab.OUTBOUND -> {
            OutboundListScreen(
                viewModel = viewModel,
                onBack = { currentTab = InventoryTab.MAIN }
            )
        }
    }
}

@Composable
private fun InventoryMainScreen(
    onAddInboundClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onOutboundClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryTextLabel(text = "Inventory Management")

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onAddInboundClick,
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
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Inbound")
                }
            }

            Button(
                onClick = onCheckoutClick,
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
                    Icon(Icons.Default.ListAlt, contentDescription = null)
                    Text("Checkout Inventory")
                }
            }
            
            Button(
                onClick = onOutboundClick,
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
                    Icon(Icons.Default.Inventory, contentDescription = null)
                    Text("Outbound Inventory")
                }
            }
            
            Button(
                onClick = onViewLogsClick,
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
                    Icon(Icons.Default.ListAlt, contentDescription = null)
                    Text("View Logs")
                }
            }
        }
    }
}

