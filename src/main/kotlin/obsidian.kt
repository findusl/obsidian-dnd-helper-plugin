@file:JsModule("obsidian")
@file:JsNonModule

import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.DocumentFragment
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import kotlin.js.Promise

open external class Component {
    open fun onload()
    open fun onunload()
}

open external class Plugin(
    app: App,
    manifest: PluginManifest
) : Component {
    open var app: App
    open var manifest: PluginManifest
    open fun addCommand(command: Command): Command
    open fun addSettingTab(settingTab: PluginSettingTab)
    open fun loadData(): Promise<Any>
    open fun saveData(data: Any): Promise<Unit>
}

external interface PluginManifest {
    var dir: String?
    var id: String
    var name: String
    var author: String
    var version: String
    var minAppVersion: String
    var description: String
    var authorUrl: String?
    var isDesktopOnly: Boolean?
}

open external class PluginSettingTab(app: App, plugin: Plugin) : SettingTab

open external class App {
    open var vault: Vault
}

external open class Vault : Events {
    open var configDir: String
    open fun getName(): String
    open fun getAbstractFileByPath(path: String): TAbstractFile?
    open fun getRoot(): TFolder
    open fun create(path: String, data: String, options: DataWriteOptions = definedExternally): Promise<TFile>
    open fun createBinary(
        path: String,
        data: ArrayBuffer,
        options: DataWriteOptions = definedExternally
    ): Promise<TFile>

    open fun createFolder(path: String): Promise<Unit>
    open fun read(file: TFile): Promise<String>
    open fun cachedRead(file: TFile): Promise<String>
    open fun readBinary(file: TFile): Promise<ArrayBuffer>
    open fun getResourcePath(file: TFile): String
    open fun delete(file: TAbstractFile, force: Boolean = definedExternally): Promise<Unit>
    open fun trash(file: TAbstractFile, system: Boolean): Promise<Unit>
    open fun rename(file: TAbstractFile, newPath: String): Promise<Unit>
    open fun modify(file: TFile, data: String, options: DataWriteOptions = definedExternally): Promise<Unit>
    open fun modifyBinary(file: TFile, data: ArrayBuffer, options: DataWriteOptions = definedExternally): Promise<Unit>
    open fun copy(file: TFile, newPath: String): Promise<TFile>
    open fun getAllLoadedFiles(): Array<TAbstractFile>
    open fun getMarkdownFiles(): Array<TFile>
    open fun getFiles(): Array<TFile>
    open fun on(
        name: String /* "create" | "modify" | "delete" */,
        callback: (file: TAbstractFile) -> Any,
        ctx: Any = definedExternally
    ): EventRef

    open fun on(name: String /* "create" | "modify" | "delete" */, callback: (file: TAbstractFile) -> Any): EventRef
    open fun on(
        name: String /* "rename" */,
        callback: (file: TAbstractFile, oldPath: String) -> Any,
        ctx: Any = definedExternally
    ): EventRef

    open fun on(name: String /* "rename" */, callback: (file: TAbstractFile, oldPath: String) -> Any): EventRef
    open fun on(name: String /* "closed" */, callback: () -> Any, ctx: Any = definedExternally): EventRef
    open fun on(name: String /* "closed" */, callback: () -> Any): EventRef

    companion object {
        fun recurseChildren(root: TFolder, cb: (file: TAbstractFile) -> Any)
    }
}

external interface DataWriteOptions {
    var ctime: Number?
    var mtime: Number?
}

external open class Events {
    open fun on(name: String, callback: (data: Any) -> Any, ctx: Any = definedExternally): EventRef
    open fun off(name: String, callback: (data: Any) -> Any)
    open fun offref(ref: EventRef)
    open fun trigger(name: String, vararg data: Any)
    open fun tryTrigger(evt: EventRef, args: Array<Any>)
}

external open class TAbstractFile {
    open var vault: Vault
    open var path: String
    open var name: String
    open var parent: TFolder
}

external open class TFile : TAbstractFile {
    open var stat: FileStats
    open var basename: String
    open var extension: String
}

external open class TFolder : TAbstractFile {
    open var children: Array<TAbstractFile>
    open fun isRoot(): Boolean
}

external interface FileStats {
    var ctime: Number
    var mtime: Number
    var size: Number
}

external interface EventRef

open external class Notice(message: String, timeout: Number = definedExternally) {
    open fun hide()
}

open external class SettingTab {
    open var app: App
    open var containerEl: HTMLElement
    open fun display()
    open fun hide()
}

external interface Command {
    var id: String
    var name: String
    var icon: String?
    var mobileOnly: Boolean?
    var callback: (() -> Unit)?
    var checkCallback: ((checking: Boolean) -> Boolean)?
//    var editorCallback: ((editor: Editor, view: MarkdownView) -> Any)?
//    var editorCheckCallback: ((checking: Boolean, editor: Editor, view: MarkdownView) -> dynamic)?
//    var hotkeys: Array<Hotkey>?
}

open external class Setting(containerEl: HTMLElement) {
    open var settingEl: HTMLElement
    open var infoEl: HTMLElement
    open var nameEl: HTMLElement
    open var descEl: HTMLElement
    open var controlEl: HTMLElement
    open fun setName(name: String): Setting /* this */
    open fun setDesc(desc: String): Setting /* this */
    open fun setDesc(desc: DocumentFragment): Setting /* this */
    open fun setClass(cls: String): Setting /* this */
    open fun setTooltip(tooltip: String): Setting /* this */
    open fun setHeading(): Setting /* this */
    open fun setDisabled(disabled: Boolean): Setting /* this */
    open fun addText(cb: (component: TextComponent) -> Any): Setting /* this */
    open fun then(cb: (setting: Setting /* this */) -> Any): Setting /* this */
}

open external class TextComponent(containerEl: HTMLElement) : AbstractTextComponent<HTMLInputElement>

open external class AbstractTextComponent<T>(inputEl: T) : ValueComponent<String> {
    open var inputEl: T
    override fun setDisabled(disabled: Boolean): AbstractTextComponent<T> /* this */
    override fun getValue(): String
    override fun setValue(value: String): AbstractTextComponent<T> /* this */
    open fun setPlaceholder(placeholder: String): AbstractTextComponent<T> /* this */
    open fun onChanged()
    open fun onChange(callback: (value: String) -> Unit): AbstractTextComponent<T> /* this */
}

open external class ValueComponent<T> : BaseComponent {
    open fun getValue(): T
    open fun setValue(value: T): ValueComponent<T> /* this */
}

open external class BaseComponent {
    open var disabled: Boolean
    open fun then(cb: (component: BaseComponent /* this */) -> Any): BaseComponent /* this */
    open fun setDisabled(disabled: Boolean): BaseComponent /* this */
}
