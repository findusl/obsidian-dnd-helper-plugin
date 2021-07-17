package extensions

public fun String.camelCaseToCapitalizedSentenceCase(): String {
    return fold("") { s, c ->
            if (c.isUpperCase()) "$s $c"
            else s + c
        }.replaceFirstChar { c -> c.uppercase() }
}
