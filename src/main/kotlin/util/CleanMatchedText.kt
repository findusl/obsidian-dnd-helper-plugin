package util

import org.w3c.dom.parsing.DOMParser

/**
 * This cleans the given text by removing any repeating spaces and newlines and html symbols
 */
fun String.cleanHtmlText(): String {
    val unescapedText = (unescapeHTML() ?: this)
    return unescapedText.removeLineBreaks().replace(Regex("  +"), " ").trim()
}

fun String.removeLineBreaks(): String = replace("\n", "")

/**
 * Not sure under what circumstances this could return null, but it should be rare.
 */
fun String.unescapeHTML(): String? {
    val doc = DOMParser().parseFromString(this, "text/html")
    return doc.documentElement?.textContent
}
