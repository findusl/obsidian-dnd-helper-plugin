package util

/**
 * Converts CamelCase to Sentence Case with every word capitalized.
 *
 * Example:
 * "ThisIsCamelCase" is converted to "This Is Camel Case"
 */
fun String.camelCaseToCapitalizedSentenceCase(): String {
    return fold("") { s, c ->
            if (c.isUpperCase()) "$s $c"
            else s + c
        }.replaceFirstChar { c -> c.uppercase() }
}
