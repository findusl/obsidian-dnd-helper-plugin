import org.w3c.dom.parsing.DOMParser
import org.w3c.xhr.XMLHttpRequest

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class SomePlugin(
    app: Any,
    manifest: Any
) : Plugin(
    app,
    manifest
) {
    override fun onload() {
        Notice("This is a notice!")
    }

    override fun onunload() {
        Notice("Unloading plugin!")
    }
}
