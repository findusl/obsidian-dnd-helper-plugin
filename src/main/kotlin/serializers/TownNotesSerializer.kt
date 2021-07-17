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
        vault.create(townFilePath, town.toMarkdown())

        // TODO add section for npc per occupation.
        // use tags where useful. One can see nested tags in pages
        // Add forth and back links for npcs
    }
}

private fun Town.toMarkdown() = """
    # $name, $type
    [Permalink]($url)
    ### Stats
    - **Population**: $population people
    - **Size**: $size
    - **Demographics**: $demographics
    - **Wealth**: $wealth
""".trimIndent()
