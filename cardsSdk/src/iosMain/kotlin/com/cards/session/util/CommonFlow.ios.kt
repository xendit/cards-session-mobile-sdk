package com.cards.session.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

actual open class CommonFlow<T> actual constructor(
  private val flow: Flow<T>
) : Flow<T> by flow {

  fun subscribe(
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    onCollect: (T) -> Unit
  ): DisposableHandle {
    val job = coroutineScope.launch(dispatcher) {
      flow.collect(onCollect)
    }
    return DisposableHandle { job.cancel() }
  }

  fun subscribe(
    onCollect: (T) -> Unit
  ): DisposableHandle {
    // Use SupervisorJob so sibling coroutines keep running
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    return subscribe(
      coroutineScope = scope,
      dispatcher = Dispatchers.Main,
      onCollect = onCollect
    )
  }
}