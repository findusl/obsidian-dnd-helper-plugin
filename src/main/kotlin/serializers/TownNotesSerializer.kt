package serializers

import Settings
import Town
import Vault

class TownNotesSerializer(
    private val vault: Vault,
    private val settings: Settings,
    private val town: Town
) {

    fun serialize() {
        val townFolderPath = "${settings.townBasePath}/${town.name}"
        vault.createFolder(townFolderPath)
        val townFilePath = "$townFolderPath/${town.name}.md"
        vault.create(townFilePath, townTemplate(town))
    }
}

private fun townTemplate(town: Town) = """
    # ${town.name}, ${town.type}
    [Permalink](${town.url})
    ### Stats
    - **Population**: ${town.population} people
    - **Size**: ${town.size}
    - **Demographics**: ${town.demographics}
    - **Wealth**: ${town.wealth}
""".trimIndent()
