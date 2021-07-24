package serializers

import models.Service
import models.Town
import util.plus

class ServiceMarkdownSerializer(private val town: Town): AbstractMarkdownSerializer() {

    fun serialize(service: Service): String {
        val sb = StringBuilder()
            .append(service.generateHeader())
            .append(service.generateMapSection())
            .append(service.generateDescriptionSection())
            .append(service.generateLocationDescriptionSection())
            .append(service.generateSpecialsSection())
            .append(service.generatePatronsSection())

        var result = sb.toString()

        result = result.withCharacterReferencesAsLinks(town.characters)

        return result
    }

    private fun Service.generateHeader() = """
        # $name, $type
    """.trimIndent().appendSectionBreak()

    private fun Service.generateMapSection() = """
        ## General
        - **Owner:** $owner
        - **Map:** [MapLink]($mapLink)
    """.trimIndent().appendSectionBreak()

    private fun Service.generateDescriptionSection() = """
        ## Description
        $description
    """.trimIndent().appendSectionBreak()

    private fun Service.generateLocationDescriptionSection(): String {
        if (locationDescription == null) return ""
        return """
            ## Location
            $locationDescription
        """.trimIndent().appendSectionBreak()
    }

    private fun Service.generateSpecialsSection() =
        specials?.fold(StringBuilder("## Specials:")) { sb, special ->
            sb + "\n- $special"
        }?.toString()?.appendSectionBreak() ?: ""

    private fun Service.generatePatronsSection() =
        patrons?.fold(StringBuilder("## Patrons:")) { sb, patron ->
            sb + "\n- $patron"
        }?.toString()?.appendSectionBreak() ?: ""

}
