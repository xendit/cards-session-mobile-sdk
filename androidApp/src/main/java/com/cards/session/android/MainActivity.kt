package com.cards.session.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
//      val viewModel = hiltViewModel<NameOfViewModel>()
//      val state by viewModel.state.collectAsState()
      Text("Hello cards sdk")
    }
  }
}
