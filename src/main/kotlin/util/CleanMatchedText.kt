package util

import org.w3c.dom.parsing.DOMParser
import parsers.handle

/**
 * This cleans the given text by removing any repeating spaces and newlines
 */
fun String.cleanHtmlText(): String =
    removeLineBreaks().replace(Regex("  +"), " ").trim()

fun String.removeLineBreaks(): String = replace("\n", "")

fun String.unescapeHTML(): String {
    val doc = DOMParser().parseFromString(this, "text/html")
    return doc.documentElement?.textContent.handle("Unescaped HTML of $this")
}
