package extensions

/**
 * Immediately takes the first element from the sequence and passes it to [consumer]. Then returns the remaining sequence.
 * This is not a lazy operation
 */
fun <T> Sequence<T>.consumeOne(consumer: (T) -> Unit): Sequence<T> {
    return ConsumeOneSequence(this, consumer)
}

internal class ConsumeOneSequence<T>(
    sequence: Sequence<T>,
    consumer: (T) -> Unit
): Sequence<T> {
    private val iterator = sequence.iterator()
    init {
        // should trigger the appropriate exception if not present
        consumer(iterator.next())
    }

    override fun iterator(): Iterator<T> = iterator
}
