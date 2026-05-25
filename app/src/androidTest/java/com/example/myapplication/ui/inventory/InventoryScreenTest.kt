package com.example.myapplication.ui.inventory

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventoryScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun inventoryActionButtons_renderAndCallHandlers() {
        var inboundClicked = false
        var checkoutClicked = false
        var outboundClicked = false
        var logsClicked = false
        var logoutClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                InventoryMainScreen(
                    onAddInboundClick = { inboundClicked = true },
                    onCheckoutClick = { checkoutClicked = true },
                    onViewLogsClick = { logsClicked = true },
                    onOutboundClick = { outboundClicked = true },
                    onLogout = { logoutClicked = true },
                    role = UserRole.ADMIN
                )
            }
        }

        composeTestRule.onNodeWithTag("InventoryActionAddInbound").performClick()
        composeTestRule.onNodeWithTag("InventoryActionCheckout").performClick()
        composeTestRule.onNodeWithTag("InventoryActionOutbound").performClick()
        composeTestRule.onNodeWithTag("InventoryActionLogs").performClick()

        assertTrue(inboundClicked)
        assertTrue(checkoutClicked)
        assertTrue(outboundClicked)
        assertTrue(logsClicked)
    }
}
