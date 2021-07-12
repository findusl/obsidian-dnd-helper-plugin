@file:JsModule("obsidian")
@file:JsNonModule

open external class Component {
    open fun onload()
    open fun onunload()
}

open external class Plugin(
    app: Any,
    manifest: Any
) : Component {
    open fun addCommand(command: Command): Command
}

open external class Notice(message: String, timeout: Number = definedExternally) {
    open fun hide()
}

external interface Command {
    var id: String
    var name: String
    var icon: String?
    var mobileOnly: Boolean?
    var callback: (() -> Any)?
    var checkCallback: ((checking: Boolean) -> dynamic)?
//    var editorCallback: ((editor: Editor, view: MarkdownView) -> Any)?
//    var editorCheckCallback: ((checking: Boolean, editor: Editor, view: MarkdownView) -> dynamic)?
//    var hotkeys: Array<Hotkey>?
}
