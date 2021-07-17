import extensions.CommandImpl
import kotlinx.coroutines.*
import parsers.KassoonTownParser
import serializers.TownNotesSerializer

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED") // works just fine, stop complaining
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class DndPlugin(app: App, manifest: PluginManifest) : Plugin(app, manifest) {
    // I doubt this has much effect in Javascript, but maybe it cancels something
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val websiteLoader = WebsiteLoader()

    @Suppress("NON_EXPORTABLE_TYPE", "UNUSED_PARAMETER") // I'm not exporting it, just used internally
    var settings: Settings = Settings()
        set(value) = coroutineScope.launch { saveSettings() }.run {  } // how to return unit from this otherwise?

    override fun onload() {
        coroutineScope.launch {
            loadSettings()
        }

        Notice("Loading D&D Generator plugin!")

        addSettingTab(SettingsTab(app, this))

        addCommand(
            CommandImpl(
            id = "dnd-generate-town",
            name = "Generate random town",
            icon = "dice",
            callback = this::generateRandomTown
        )
        )
    }

    private fun generateRandomTown() = coroutineScope.launch {
        try {
            val town = parseKassoonTownWebsite("/dnd/town-generator/10/518707/")
            TownNotesSerializer(app.vault, settings, town).serialize()
        } catch (e: Exception) {
            Notice("There was an error trying to parse the town. Maybe the Kassoon website changed slightly, that breaks it.")
            e.printStackTrace()
        }
    }

    override fun onunload() {
        Notice("Unloading plugin!")
        coroutineScope.cancel("Unloading plugin.")
    }

    private suspend fun loadSettings() {
        this.settings = this.loadData().await() as? Settings ?: Settings()
    }

    private suspend fun saveSettings() {
        saveData(this.settings).await()
    }

    private suspend fun parseKassoonTownWebsite(url: String): Town {
        val document = websiteLoader.loadKassoonWebsite(url)
        println("Got document from $url")
        val townParser = KassoonTownParser()
        return townParser.parseKassoonTown(document)
    }
}
