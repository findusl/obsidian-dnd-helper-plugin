package serializers

import models.Character

object CharacterMarkdownSerializer: AbstractMarkdownSerializer() {
    fun serialize(character: Character): String {
        val sb = StringBuilder()
            .append(character.generateHeader())
            .append(character.generateStatsSection())
            .append(character.generateDescriptionSection())
            .append(character.generatePersonalitySection())
            .append(character.generateMotivationSection())
            .append(character.generateHistorySection())

        return sb.toString()
    }

    private fun Character.generateHeader() = """
        # $name
        [Permalink]($url)
    """.trimIndent().appendSectionBreak()

    private fun Character.generateStatsSection() = """
        ## Stats
        - **Occupation**: #$occupation
        - **Race**: #$race
        - **Gender**: $gender
        - **Voice**: $voice
        - **Seen in Places**: 
    """.trimIndent().appendSectionBreak()

    private fun Character.generatePersonalitySection(): String {
        var sb = StringBuilder("## Personality")
        if (ideals != null)
            sb = sb.append("\n- **Ideals**: ").append(ideals)
        if (flaws != null)
            sb = sb.append("\n- **Flaws**: ").append(flaws)
        if (bonds != null)
            sb = sb.append("\n- **Bonds**: ").append(bonds)

        sb.append("\n- **Personality**: ").append(personality)

        return sb.toString().appendSectionBreak()
    }

    private fun Character.generateMotivationSection() = """
        ## Motivation
        $motivation
    """.trimIndent().appendSectionBreak()

    private fun Character.generateDescriptionSection() = """
        ## Description
        $description
    """.trimIndent().appendSectionBreak()

    private fun Character.generateHistorySection() = """
        ## History
        $history
    """.trimIndent().appendSectionBreak()
}