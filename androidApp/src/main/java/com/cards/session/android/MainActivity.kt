package com.cards.session.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cards.session.android.view.AndroidCardSessionViewModel
import com.cards.session.cards.ui.CardSessionEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
  val navController = rememberNavController()
  NavHost(
    navController = navController,
    startDestination = "TODO"
  ) {
    composable(route = "TODO") {
      val viewModel = hiltViewModel<AndroidCardSessionViewModel>()
      val state by viewModel.state.collectAsState()
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
          text = "Welcome to Cards Session!",
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(content = { Text(text = "Collect Card Data") }, onClick = {
          viewModel.onEvent(
            CardSessionEvent.CollectCardData(
              cardNumber = "4242424242424242",
              expiryMonth = "12/22",
              expiryYear = "2026",
              cardholderFirstName = "First",
              cardholderLastName = "Name",
              cardholderEmail = "firstname@xendit.co",
              cardholderPhoneNumber = "01231245242",
              paymentSessionId = "1234567890",
              deviceFingerprint = "1234567890"
            )
          )
        })
        Spacer(modifier = Modifier.height(16.dp))
        Button(content = { Text(text = "Collect CVN") }, onClick = {
          viewModel.onEvent(
            CardSessionEvent.CollectCvn(
              cvn = "123",
              paymentSessionId = "1234567890",
              deviceFingerprint = "1234567890"
            )
          )
        })
        Spacer(modifier = Modifier.height(16.dp))
        if (state.isLoading) {
          Text(text = "Loading...")
        } else if (state.error != null) {
          Text(text = "Error: ${state.error}", color = Color.Red)
        } else if (state.cardResponse != null) {
          Text(text = "Card Response: ${state.cardResponse}")
        }
      }
    }
  }
}
