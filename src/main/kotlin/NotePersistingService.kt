import models.Service
import models.Town
import serializers.ServiceMarkdownSerializer
import serializers.TownMarkdownSerializer
import settings.Settings

// TODO handle file collisions
class NotePersistingService(
    private val vault: Vault,
    private val settings: Settings
) {

    fun persistTown(town: Town) {
        val sanitizedTownName = town.name.sanitizeFileName()
        val townFolderPath = "${settings.townBasePath}/${sanitizedTownName}"
        vault.createFolder(townFolderPath)

        val townFilePath = "$townFolderPath/${sanitizedTownName}.md"
        val townFileContent = TownMarkdownSerializer.serialize(town)
        vault.create(townFilePath, townFileContent)

        val serviceMarkdownSerializer = ServiceMarkdownSerializer(town)
        town.services.forEach { service ->
            persistService(service, townFolderPath, serviceMarkdownSerializer)
        }
    }

    private fun persistService(service: Service, townFolderPath: String, serializer: ServiceMarkdownSerializer) {
        val sanitizedServiceName = service.name.sanitizeFileName()
        val serviceFilePath = "$townFolderPath/${sanitizedServiceName}.md"
        val serviceFileContent = serializer.serialize(service)
        vault.create(serviceFilePath, serviceFileContent)
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
