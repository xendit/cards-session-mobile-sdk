package com.cards.session.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cards.session.cards.sdk.CardSessions
import com.cards.session.cards.sdk.create
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
      apiKey = "xnd_public_development_YOUR_KEY_HERE"
    )
  }
  val state by cardSessions.state.collectAsState()
  val scope = CoroutineScope(Dispatchers.Main)
  var paymentSessionId = ""
  var isConfirmedSaved by remember { mutableStateOf(false) }

  NavHost(
    navController = navController,
    startDestination = "cards"
  ) {
    composable(route = "cards") {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
          text = "Welcome to Cards Session!",
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
          label = "Enter paymentSessionId",
          placeholder = "paymentSessionId",
          onValueChange = { newText ->
            // Handle text change here
            paymentSessionId = newText
            println("New text: $newText")
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Confirm Save",
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
          )

          Switch(
            checked = isConfirmedSaved,
            onCheckedChange = {
              isConfirmedSaved = it
            }
            )
        }


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
                cardholderPhoneNumber = "+123456789",
                paymentSessionId = paymentSessionId,
                confirmSave = isConfirmedSaved,
                billingInformation = com.cards.session.cards.models.BillingInformationDto(
                  firstName = "Budi",
                  lastName = "Santoso",
                  email = "budi@example.co.id",
                  phoneNumber = "+6281234567890",
                  streetLine1 = "Jl. Jend. Sudirman No.Kav 48A",
                  streetLine2 = "",
                  city = "Jakarta",
                  provinceState = "DKI Jakarta",
                  country = "ID",
                  postalCode = "12190"
                )
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
                paymentSessionId = paymentSessionId
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
          Text(text = state.cardResponse?.message ?: "Success")
        }
      }
    }
  }
}
