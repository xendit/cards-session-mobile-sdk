package com.cards.session.util

import kotlinx.coroutines.flow.MutableStateFlow

expect class CommonMutableStateFlow<T>(flow: MutableStateFlow<T>) : MutableStateFlow<T>

fun <T> MutableStateFlow<T>.toCommonMutableStateFlow() =
  com.cards.session.util.CommonMutableStateFlow(this)