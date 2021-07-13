import kotlinx.browser.window
import org.w3c.dom.parsing.DOMParser
import org.w3c.fetch.Request
import serializers.TownNotesSerializer

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED") // works just fine, stop complaining
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class DndPlugin(app: App, manifest: PluginManifest) : Plugin(app, manifest) {

    @Suppress("NON_EXPORTABLE_TYPE") // I'm not exporting it, just used internally
    // TODO move logic to different file to limit what is exported. maybe with delegation
    var settings: Settings = Settings()
        set(value) = saveSettings()

    override fun onload() {
        this.loadSettings()

        Notice("Loading D&D Generator plugin!")

        addSettingTab(SettingsTab(app, this))

        addCommand(CommandImpl(
            id = "dnd-generate-town",
            name = "Generate random town",
            icon = "dice",
            callback = {
                parseTownWebsite("http://localhost:8010/proxy/dnd/town-generator/10/518707/")
            }
        ))
    }

    override fun onunload() {
        Notice("Unloading plugin!")
    }

    private fun parseTownWebsite(url: String) {
        loadWebsite(url) {
            val document = DOMParser().parseFromString(it, "text/html")
            val town = document.parseKassoonTown()
            TownNotesSerializer(app.vault, settings, town).serialize()
        }
    }

    private fun loadSettings() {
        this.loadData().then {
            this.settings = it as? Settings ?: Settings()
        }
    }
    /*
    TODO see if it works with coroutines like this:
     suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
        then({ cont.resume(it) }, { cont.resumeWithException(it) })
    }
     */

    private fun saveSettings() {
        saveData(this.settings);
    }
}

private fun loadWebsite(url: String, handler: (String) -> Unit) {
    window.fetch(Request(url)).then(onFulfilled = {
        it.text().then(onFulfilled = {
            handler(it)
        })
    })
}
