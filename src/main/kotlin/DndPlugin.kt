import dependencies.*
import kotlinx.coroutines.*
import parsers.KassoonCharacterParser
import parsers.KassoonTownParser
import settings.Settings
import settings.SettingsTab
import ui.InputModal
import util.*

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED") // works just fine, stop complaining
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class DndPlugin(app: App, manifest: PluginManifest) : Plugin(app, manifest) {
    // use https://kt.academy/article/cc-exception-handling
    private val  coroutineExceptionHandler = CoroutineExceptionHandler {
            _, exception ->
        Notice("Something went badly wrong. Please send me the console log of obsidian.")
        exception.printStackTrace()
    }

    // Can be used to cancel all ongoing operations.
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob() + coroutineExceptionHandler)

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

        addSettingTab(SettingsTab(app, this))

        addGenerateTownCommand()

        addCleanCommand()

        addTestStuffCommand()
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
                id = "dnd-import-url",
                name = "Import Dnd url",
                icon = "dice",
                callback = this::importDndUrl
            )
        )
    }

    private fun addTestStuffCommand() {
        addCommand(
            CommandImpl(
                id = "dnd-generate-test",
                name = "Test some command",
                icon = "dice",
                callback = this::testStuff
            )
        )
    }

    private fun testStuff() = coroutineScope.launch {
        Notice("first")
        Notice("second")
    }

    private fun importDndUrl() = coroutineScope.launch {
        // TODO move to seperate class
        try {
            val url = InputModal(app).openForResult().trim()
            val logger = identifyImportAndPersistUrl(url)
            if (logger.didLogError)
                Notice("Not everything worked. Please send me the console log of obsidian.")
        } catch (e: AlreadyLoggedException) {
            Notice("Parsing error. Please send me the console log of obsidian.")
        } catch (e: Error) {
            Notice("Parsing error. Got unhandled exception. Please send me the console log of obsidian.")
        }
    }

    private suspend fun identifyImportAndPersistUrl(url: String): StepAwareLogger {
        val logger: StepAwareLogger
        if (url.contains("town-generator")) {
            logger = importAndPersistTown(url)
        } else if(url.contains("npc-generator")) {
            logger = importAndPersistCharacter(url)
        } else {
            logger = StepAwareLogger("Character $url", app = app)
            logger.logAndShow("Unknown URL. Currently the tool only supports the kassoon Town generator or npc-generator.")
        }
        return logger
    }

    private val userCorsMessage = "Couldn't reach the website. The Tool requires a CORS Proxy running on 8010 as described in the README. " +
            "If you have the CORS proxy running then please send me the log output of obsidian for further analysis."

    private suspend fun importAndPersistCharacter(url: String): StepAwareLogger {
        val logger = StepAwareLogger("Character $url", app = app)
        val document = try {
            websiteLoader.loadWebsite(url)
        } catch (e: Throwable) {
            logger.logAndShow(userCorsMessage, e.message)
            return logger
        }
        val characterParser = KassoonCharacterParser(document, logger)
        val character = characterParser.parseKassoonCharacter()
        coroutineScope.assertNotCancelled()
        lastCreatedFiles = NotePersistingService(app.vault, settings).persistCharacter(character, logger)
        return logger
    }

    private suspend fun importAndPersistTown(url: String): StepAwareLogger {
        val logger = StepAwareLogger("Town $url", app = app)
        val document = try {
            websiteLoader.loadWebsite(url)
        } catch (e: Throwable) {
            logger.logAndShow(userCorsMessage, e.message)
            return logger
        }
        val townParser = KassoonTownParser(document, logger)
        val town = townParser.parseKassoonTown()
        coroutineScope.assertNotCancelled()
        lastCreatedFiles = NotePersistingService(app.vault, settings).persistTown(town, logger)
        return logger
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
        console.log("Loaded settings: $loaded")
        val loadedString = loaded as? String ?: return
        this.settings = JSON.parse(loadedString)
    }

    private suspend fun saveSettings() {
        saveData(JSON.stringify(settings)).await()
    }
}
