package serializers

import models.Town
import util.plus

object TownMarkdownSerializer: AbstractMarkdownSerializer() {

    fun serialize(town: Town): String {
        val sb = StringBuilder()
            .append(town.generateHeader())
            .append(town.generateStatsSection())
            .append(town.generateDescriptionSection())
            .append(town.generateDefensesSection())
            .append(town.generateServicesSection())

        var result = sb.toString()

        result = result.withCharacterReferencesAsLinks(town.characters)

        return result
    }

    private fun Town.generateHeader() = """
        # $name, $type
        [Permalink]($url)
    """.trimIndent().appendSectionBreak()

    private fun Town.generateStatsSection() = """
        ## Stats
        - **Population**: $population
        - **Size**: $size
        - **Demographics**: $demographics
        - **Wealth**: $wealth
    """.trimIndent().appendSectionBreak()

    private fun Town.generateDescriptionSection() = """
        ## Description
        $description
    """.trimIndent().appendSectionBreak()

    private fun Town.generateDefensesSection() = """
        ## Defenses
        $defenses
    """.trimIndent().appendSectionBreak()

    private fun Town.generateServicesSection() =
        services.fold(StringBuilder("## Services:")) { sb, service ->
            sb + "\n- **${service.type}**: [[${service.name}]]"
        }.toString().appendSectionBreak()

}
