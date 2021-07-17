package parsers

internal fun String?.handle(description: String = ""): String {
    // TODO should probably be more resilient in case website changes. At least extract what is possible and collect errors
    return this ?: throw NullPointerException("Expected string value for $description")
}