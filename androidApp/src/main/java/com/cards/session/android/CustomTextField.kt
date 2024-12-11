package com.cards.session.android

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
  label: String,
  placeholder: String = "",
  onValueChange: (String) -> Unit
) {
  var text by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    OutlinedTextField(
      value = text,
      onValueChange = { newValue ->
        text = newValue
        onValueChange(newValue)
      },
      label = { Text(text = label) },
      placeholder = { Text(text = placeholder) },
      modifier = Modifier.fillMaxWidth()
    )
  }
}