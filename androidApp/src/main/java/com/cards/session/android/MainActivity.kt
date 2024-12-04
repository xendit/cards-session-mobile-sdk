package com.cards.session.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cards.session.cards.sdk.CardSessions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          AppRoot()
        }
      }
    }
  }
}

@Composable
fun AppRoot() {
  val context = LocalContext.current
  val navController = rememberNavController()
  val cardSessions = remember {
    CardSessions.create(
      context = context,
      apiKey = "API_KEY_HERE"
    )
  }
  val state by cardSessions.state.collectAsState()
  val scope = CoroutineScope(Dispatchers.Main)

  NavHost(
    navController = navController,
    startDestination = "cards"
  ) {
    composable(route = "cards") {
      Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
          text = "Welcome to Cards Session!",
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          content = { Text(text = "Collect Card Data") },
          onClick = {
            scope.launch {
              cardSessions.collectCardData(
                cardNumber = "4242424242424242",
                expiryMonth = "12",
                expiryYear = "2026",
                cvn = null,
                cardholderFirstName = "First",
                cardholderLastName = "Name",
                cardholderEmail = "firstname@xendit.co",
                cardholderPhoneNumber = "01231245242",
                paymentSessionId = "session_id MUST be 27 chars"
              )
            }
          }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          content = { Text(text = "Collect CVN") },
          onClick = {
            scope.launch {
              cardSessions.collectCvn(
                cvn = "123",
                paymentSessionId = "1234567890"
              )
            }
          }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (state.isLoading) {
          Text(text = "Loading...")
        } else if (state.exception != null) {
          val errorMessage = state.exception?.message ?: "Error: ${state.exception?.errorCode}"
          Text(text = errorMessage, color = Color.Red)
        } else if (state.cardResponse != null) {
          Text(text = "Card Response: ${state.cardResponse}")
        }
      }
    }
  }
}
