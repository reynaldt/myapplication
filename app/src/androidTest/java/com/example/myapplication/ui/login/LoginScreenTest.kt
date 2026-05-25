package com.example.myapplication.ui.login

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.domain.model.LoggedInUser
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class LoginScreenTest {

    @get:Rule
    val composeTestRule: ComposeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sessionManager = SessionManager(context)
        viewModel = LoginViewModel(FakeAuthRepository(), sessionManager)
    }

    @Test
    fun loginButton_isDisabledUntilFieldsAreFilled() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LoginScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("LoginButton").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("LoginUsername").performTextInput("admin")
        composeTestRule.onNodeWithTag("LoginPassword").performTextInput("admin")
        composeTestRule.onNodeWithTag("LoginButton").assertIsEnabled()
    }

    @Test
    fun loginSuccess_triggersOnLoginSuccess() {
        val loginSuccess = AtomicBoolean(false)

        composeTestRule.setContent {
            MyApplicationTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { loginSuccess.set(true) }
                )
            }
        }

        composeTestRule.onNodeWithTag("LoginUsername").performTextInput("admin")
        composeTestRule.onNodeWithTag("LoginPassword").performTextInput("password")
        composeTestRule.onNodeWithTag("LoginButton").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) { loginSuccess.get() }
        assertTrue(loginSuccess.get())
    }

    private class FakeAuthRepository : AuthRepository {
        override suspend fun login(username: String, password: String) =
            if (username == "admin" && password == "password") {
                Result.success(
                    LoggedInUser(
                        id = "user-1",
                        username = "admin",
                        displayName = "Admin User",
                        role = com.example.myapplication.domain.model.UserRole.ADMIN
                    )
                )
            } else {
                Result.failure(Exception("Invalid credentials"))
            }

        override fun logout() {}

        override fun getCurrentUser() = null
    }
}
