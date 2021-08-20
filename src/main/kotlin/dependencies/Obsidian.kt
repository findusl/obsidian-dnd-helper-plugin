@file:JsModule("obsidian")
@file:JsNonModule

package dependencies

import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.DocumentFragment
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
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
    open fun addCommand(command: Command): Command
    open fun addSettingTab(settingTab: PluginSettingTab)
    open fun loadData(): Promise<Any>
    open fun saveData(data: Any): Promise<Unit>
    open fun addStatusBarItem(): HTMLElement
}

external interface PluginManifest {
}

open external class PluginSettingTab(app: App, plugin: Plugin) : SettingTab

open external class App {
    open var vault: Vault
}

open external class Vault : Events {
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

open external class Events {
    open fun on(name: String, callback: (data: Any) -> Any, ctx: Any = definedExternally): EventRef
    open fun off(name: String, callback: (data: Any) -> Any)
    open fun offref(ref: EventRef)
    open fun trigger(name: String, vararg data: Any)
    open fun tryTrigger(evt: EventRef, args: Array<Any>)
}

open external class TAbstractFile {
    open var vault: Vault
    open var path: String
    open var name: String
    open var parent: TFolder
}

open external class TFile : TAbstractFile {
    open var stat: FileStats
    open var basename: String
    open var extension: String
}

open external class TFolder : TAbstractFile {
    open var children: Array<TAbstractFile>
    open fun isRoot(): Boolean
}

external interface FileStats {
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

external interface CloseableComponent {
    fun close(): Any
}

open external class Modal(app: App) : CloseableComponent {
    open var app: App
    open var containerEl: HTMLElement
    open var modalEl: HTMLElement
    open var titleEl: HTMLElement
    open var contentEl: HTMLElement
    open var shouldRestoreSelection: Boolean
    open fun open()
    override fun close()
    open fun onOpen()
    open fun onClose()
}

external interface ISuggestOwner<T> {
    fun renderSuggestion(value: T, el: HTMLElement)
    fun selectSuggestion(value: T, evt: MouseEvent)
    fun selectSuggestion(value: T, evt: KeyboardEvent)
}

open external class SuggestModal<T>(app: App) : Modal, ISuggestOwner<T> {
    open var limit: Number
    open var emptyStateText: String
    open var inputEl: HTMLInputElement
    open var resultContainerEl: HTMLElement
    open fun setPlaceholder(placeholder: String)
    open fun onNoSuggestion()
    override fun selectSuggestion(value: T, evt: MouseEvent)
    override fun selectSuggestion(value: T, evt: KeyboardEvent)
    open fun getSuggestions(query: String): Array<T>
    override fun renderSuggestion(value: T, el: HTMLElement)
    open fun onChooseSuggestion(item: T, evt: MouseEvent)
    open fun onChooseSuggestion(item: T, evt: KeyboardEvent)
}
