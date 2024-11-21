package com.cards.session.util

import kotlinx.coroutines.flow.MutableStateFlow

class IOSMutableStateFlow<T>(
  initialValue: T
) : com.cards.session.util.CommonMutableStateFlow<T>(MutableStateFlow(initialValue))