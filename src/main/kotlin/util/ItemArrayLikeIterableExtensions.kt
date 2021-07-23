package util

import org.w3c.dom.ItemArrayLike

fun <T> ItemArrayLike<T>.iterator(): Iterator<T> = IteratorFromItemArrayLike(this)

internal class IteratorFromItemArrayLike<T>(
    private val array: ItemArrayLike<T>
) : Iterator<T> {
    private var current = 0
    override fun hasNext(): Boolean {
        return current < array.length
    }

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException("Iterator has no more elements.")
        return array.item(current++) ?: throw NoSuchElementException("Iterator has no more elements.")
    }
}
