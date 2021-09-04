package util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

fun CoroutineScope.assertNotCancelled() {
    if (!isActive) throw CancellationException("Cancelled")
}
