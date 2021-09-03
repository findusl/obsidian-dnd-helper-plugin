import dependencies.Notice
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

// TODO handle file collisions
class NotePersistingService(
    private val vault: Vault,
    private val settings: Settings
) {

    private val recentlyCreated = mutableListOf<TAbstractFile>()

    /**
     * Creates the files and returns a list of created files.
     */
    suspend fun persistTown(town: Town): List<TAbstractFile> {
        val sanitizedTownName = town.name.sanitizeFileName()
        val townFolderPath = "${settings.townBasePath}/${sanitizedTownName}"
        vault.createFolder(townFolderPath).await()
        vault.getAbstractFileByPath(townFolderPath)?.let { recentlyCreated.add(it) }

        val townFilePath = "$townFolderPath/${sanitizedTownName}.md"
        val townFileContent = TownMarkdownSerializer.serialize(town)
        vault.create(townFilePath, townFileContent).await().let { recentlyCreated.add(it) }

        val serviceMarkdownSerializer = ServiceMarkdownSerializer(town)
        town.services.forEach { service ->
            persistService(service, townFolderPath, serviceMarkdownSerializer)
        }
        town.characters.forEach { character ->
            // TODO handle duplicate files. Maybe town name in brackets or regenerate in advance
            persistCharacter(character)
        }
        return recentlyCreated
    }

    private suspend fun persistService(service: Service, townFolderPath: String, serializer: ServiceMarkdownSerializer) {
        val sanitizedServiceName = service.name.sanitizeFileName()
        val serviceFilePath = "$townFolderPath/${sanitizedServiceName}.md"
        val serviceFileContent = serializer.serialize(service)
        vault.create(serviceFilePath, serviceFileContent).await().let { recentlyCreated.add(it) }
    }

    private suspend fun persistCharacter(character: Character) {
        val sanitizedCharacterName = character.name.sanitizeFileName()
        val characterFilePath = "${settings.characterBasePath}/${sanitizedCharacterName}.md"
        val characterFileContent = CharacterMarkdownSerializer.serialize(character)
        vault.create(characterFilePath, characterFileContent).await().let { recentlyCreated.add(it) }
    }

}

private fun String.sanitizeFileName(): String {
    return filter { c ->
        val legalCharacter = !ILLEGAL_FILE_NAME_CHARACTERS.contains(c)
        // TODO this probably leads to problems with linking, handle somehow
        if (!legalCharacter) {
            console.log("Had to skip character in $this")
            Notice("Illegal file name $this. Linking might not work.")
        }
        legalCharacter
    }
}

private val ILLEGAL_FILE_NAME_CHARACTERS =
    charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
