import dependencies.TAbstractFile
import dependencies.Vault
import kotlinx.coroutines.await
import models.Character
import models.Service
import models.Town
import serializers.CharacterMarkdownSerializer
import serializers.ServiceMarkdownSerializer
import serializers.TownMarkdownSerializer
import settings.Settings
import util.StepAwareLogger

// TODO handle file collisions
class NotePersistingService(
    private val vault: Vault,
    private val settings: Settings
) {

    private val recentlyCreated = mutableListOf<TAbstractFile>()

    /**
     * Creates the files and returns a list of created files.
     */
    suspend fun persistTown(town: Town, logger: StepAwareLogger): List<TAbstractFile> {
        val sanitizedTownName = town.name.sanitizeFileName(logger)
        val townFolderPath = "${settings.townBasePath}/${sanitizedTownName}"
        vault.createFolder(townFolderPath).await()
        vault.getAbstractFileByPath(townFolderPath)?.let { recentlyCreated.add(it) }

        val townFilePath = "$townFolderPath/${sanitizedTownName}.md"
        val townFileContent = TownMarkdownSerializer.serialize(town)
        vault.create(townFilePath, townFileContent).await().let { recentlyCreated.add(it) }

        val serviceMarkdownSerializer = ServiceMarkdownSerializer(town)
        town.services.forEach { service ->
            persistService(service, townFolderPath, serviceMarkdownSerializer, logger)
        }
        town.characters.forEach { character ->
            // TODO handle duplicate files. Maybe town name in brackets or regenerate in advance
            persistCharacter(character, StepAwareLogger(character.name, logger))
        }
        return recentlyCreated
    }

    private suspend fun persistService(
        service: Service,
        townFolderPath: String,
        serializer: ServiceMarkdownSerializer,
        parentLogger: StepAwareLogger
    ) {
        val logger = StepAwareLogger(service.name, parentLogger)
        try {
            val sanitizedServiceName = service.name.sanitizeFileName(logger)
            val serviceFilePath = "$townFolderPath/${sanitizedServiceName}.md"
            val serviceFileContent = serializer.serialize(service)
            vault.create(serviceFilePath, serviceFileContent).await().let { recentlyCreated.add(it) }
        } catch (e: Exception) {
            logger.logAndShow("Unable to serialize service ${service.name}. Linking might be broken", e.message)
        }
    }

    suspend fun persistCharacter(character: Character, logger: StepAwareLogger): List<TAbstractFile> {
        if (!vault.adapter.exists(settings.characterBasePath).await()) {
            vault.createFolder(settings.characterBasePath)
        }
        try {
            val sanitizedCharacterName = character.name.sanitizeFileName(logger)
            val characterFilePath = "${settings.characterBasePath}/${sanitizedCharacterName}.md"
            if (vault.adapter.exists(characterFilePath).await()) {
                console.log("File path already exists $characterFilePath")
                logger.logAndShow("Character with name ${character.name} cannot be created (file already exists)")
            } else {
                val characterFileContent = CharacterMarkdownSerializer.serialize(character)
                vault.create(characterFilePath, characterFileContent).await().let { recentlyCreated.add(it) }
            }
        } catch (e: Exception) {
            logger.logAndShow("Unable to serialize character ${character.name}. Linking between files might be broken", e.message)
        }
        return recentlyCreated
    }

    /* private suspend fun findAndReplaceDuplicateNames(character: Character) {
        TODO("To be implemented")
    }*/
}

private fun String.sanitizeFileName(logger: StepAwareLogger): String {
    return filter { c ->
        val legalCharacter = !ILLEGAL_FILE_NAME_CHARACTERS.contains(c)
        if (!legalCharacter) {
            logger.logAndShow("Illegal file name $this. Linking might not work.", "Skipped $c")
        }
        legalCharacter
    }
}

private val ILLEGAL_FILE_NAME_CHARACTERS =
    charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
