import util.CommandImpl
import kotlinx.coroutines.*
import models.Town
import parsers.KassoonTownParser
import settings.Settings
import settings.SettingsTab
import util.WebsiteLoader

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED") // works just fine, stop complaining
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class DndPlugin(app: App, manifest: PluginManifest) : Plugin(app, manifest) {
    // I doubt this has much effect in Javascript, but maybe it cancels something
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val websiteLoader = WebsiteLoader()

    private var lastCreatedFiles: List<TAbstractFile>? = null

    @Suppress("NON_EXPORTABLE_TYPE") // I'm not exporting it, just used internally
    var settings: Settings = Settings()
        set(value) {
            field = value
            coroutineScope.launch { saveSettings() }
        }

    override fun onload() {
        coroutineScope.launch {
            loadSettings()
        }

        console.log("Loading D&D Generator plugin!")

        addSettingTab(SettingsTab(app, this))

        addCommand(
            CommandImpl(
                id = "dnd-generate-town",
                name = "Generate random town",
                icon = "dice",
                callback = this::generateRandomTown
            )
        )

        addCommand(
            CommandImpl(
                id = "dnd-cleanup-last",
                name = "Cleanup last creation of files",
                icon = "trash",
                callback = this::cleanupLastGeneration
            )
        )
    }

    private fun generateRandomTown() = coroutineScope.launch {
        try {
            val town = parseKassoonTownWebsite("/dnd/town-generator/10/518707/")
            lastCreatedFiles = NotePersistingService(app.vault, settings).persistTown(town)
        } catch (e: Exception) {
            Notice("There was an error trying to parse the town. Maybe the Kassoon website changed slightly, that breaks it.")
            e.printStackTrace()
        }
    }

    private fun cleanupLastGeneration() = coroutineScope.launch {
        lastCreatedFiles?.reversed()?.forEach {
            try {
                app.vault.delete(it).await()
            } catch (e: Exception) {
                console.log("Could not delete file ${it.path}")
            }
        }
    }

    override fun onunload() {
        Notice("Unloading plugin!")
        coroutineScope.cancel("Unloading plugin.")
    }

    private suspend fun loadSettings() {
        val loaded = loadData().await()
        console.log("Loaded $loaded")
        val loadedString = loaded as? String ?: return
        this.settings = JSON.parse(loadedString)
    }

    private suspend fun saveSettings() {
        saveData(JSON.stringify(settings)).await()
    }

    private suspend fun parseKassoonTownWebsite(url: String): Town {
        val document = websiteLoader.loadKassoonWebsite(url)
        println("Got document from $url")
        val townParser = KassoonTownParser(document)
        return townParser.parseKassoonTown()
    }
}
