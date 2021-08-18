import util.CommandImpl
import kotlinx.coroutines.*
import models.Town
import parsers.KassoonTownParser
import settings.Settings
import settings.SettingsTab
import util.AlreadyLoggedException
import util.StepAwareLogger
import util.WebsiteLoader

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED") // works just fine, stop complaining
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class DndPlugin(app: App, manifest: PluginManifest) : Plugin(app, manifest) {
    // Can be used to cancel all ongoing operations.
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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

        addGenerateTownCommand()

        addCleanCommand()
    }

    private fun addCleanCommand() {
        addCommand(
            CommandImpl(
                id = "dnd-cleanup-last",
                name = "Cleanup last creation of files",
                icon = "trash",
                callback = this::cleanupLastGeneration
            )
        )
    }

    private fun addGenerateTownCommand() {
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
            val url = "/dnd/town-generator/10/518707/"
            val logger = StepAwareLogger("Character $url")
            val town = parseKassoonTownWebsite(url, logger)
            if (!this.isActive) throw CancellationException("Cancelled")
            lastCreatedFiles = NotePersistingService(app.vault, settings).persistTown(town)
            if (logger.didLogError)
                Notice("Not everything worked. Please send me the console log of obsidian.")
        } catch (e: AlreadyLoggedException) {
            Notice("Parsing error. Please send me the console log of obsidian.")
        } catch (e: Exception) {
            Notice("Something went badly wrong. Please send me the console log of obsidian.")
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

    private suspend fun parseKassoonTownWebsite(url: String, logger: StepAwareLogger): Town {
        val document = websiteLoader.loadKassoonWebsite(url)
        println("Got document from $url")
        val townParser = KassoonTownParser(document, logger)
        return townParser.parseKassoonTown()
    }
}
