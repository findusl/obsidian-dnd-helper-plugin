package extensions

/**
 * This cleans the given text by removing any repeating spaces and newlines
 */
fun String.cleanHtmlText(): String =
    removeLineBreaks().replace(Regex("  +"), " ").trim()

fun String.removeLineBreaks(): String = replace("\n", "")
