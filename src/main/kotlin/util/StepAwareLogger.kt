package util


open class StepAwareLogger(
    private val stepName: String = "Root step",
    private val parentLogger: StepAwareLogger? = null
) {
    open var didLogError = false
        protected set

    private fun generateLogMessagePrefix(): String {
        val prefix = parentLogger?.generateLogMessagePrefix() ?: ""
        return "$prefix$stepName -> "
    }

    open fun logError(message: String) {
        rememberErrorWasLogged()
        console.error(generateLogMessagePrefix() + message)
    }

    open fun logAndThrow(message: String): Nothing {
        rememberErrorWasLogged()
        console.error(generateLogMessagePrefix() + message)
        throw AlreadyLoggedException()
    }

    fun <T: Any> logIfNull(value: T?, variableName: String): T? {
        if (value == null)
            logError("$variableName is null")
        return value
    }

    fun <T: Any> logIfNullAndFallback(value: T?, variableName: String, default: T): T {
        if (value == null) {
            logError("$variableName is null")
            return default
        }
        return value
    }

    fun logIfNullAndDefaultFallback(value: String?, variableName: String): String {
        return logIfNullAndFallback(value, variableName, SERIALIZATION_ERROR_PLACEHOLDER)
    }

    private fun rememberErrorWasLogged() {
        parentLogger?.rememberErrorWasLogged()
        didLogError = true
    }

}

class AlreadyLoggedException: RuntimeException() {

}
