package serializers

import models.Character

abstract class AbstractMarkdownSerializer {
    protected fun String.withCharacterReferencesAsLinks(characters: List<Character>): String {
        var result = this
        characters.forEach { character ->
            console.log("Trying to replace ${character.name}")
            result = result.replace(character.name, "[[${character.name}]]")
        }
        return result
    }

    protected fun String.appendSectionBreak(): String = this + "\n\n"
}
