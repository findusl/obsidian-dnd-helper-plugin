package parsers

import Character
import Notice
import WebsiteLoader
import extensions.iterator
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.w3c.dom.Document
import org.w3c.dom.Element

internal class CharacterBuilder {
    lateinit var name: String
    lateinit var race: String
    lateinit var gender: String
    lateinit var occupation: String

    fun build(): Character {
        // throws exceptions if not all values were set
        return Character(name, race, gender, occupation)
    }
}

suspend fun Element.detectCharacters(): List<Character> {
    val characterRegex = Regex("""["']+(/\?page=dnd&subpage=npc-generator&[^"']*)["']""")
    return characterRegex.findAll(innerHTML).asFlow()
        .map {
            // TODO extract WebsiteLoader to be injectable
            val characterDocument = WebsiteLoader().loadKassoonWebsite(it.groupValues[1])
            characterDocument.parseKassoonCharacter()
        }
        .toList()
}

fun Document.parseKassoonCharacter(): Character {
    val contentNode = getElementById("content") ?: throw NoSuchElementException("Missing content.")
    val nodes = contentNode.children
    console.log("CharacterParser: There are ${nodes.length} nodes")
    val characterBuilder = CharacterBuilder()
    nodes.iterator().asSequence()
    // TODO parse character
    // TODO correct for NPC occupation in Sherif and Town leader

    val result = characterBuilder.build()
    Notice("Result of Character parsing: $result")
    return result
}
