package parsers

import Character
import Notice
import WebsiteLoader
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.ParentNode
import util.consumeOne
import util.iterator
import util.matchOrThrow
import util.unescapeHTML

class KassoonCharacterParser(private val document: Document): AbstractCharacterParser() {

    private val characterBuilder = CharacterBuilder()

    fun parseKassoonCharacter(): Character {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE") // why would parentNode not be a ParentNode?
        val contentNode = document.getElementById("npcTitle")?.parentNode as? ParentNode
            ?: throw NoSuchElementException("Missing content.")

        val nodes = contentNode.children
        console.log("TownParser: There are ${nodes.length} nodes")
        nodes.iterator().asSequence()
            .consumeCharacterHeader()
            .dropWhile { !"p3".equals(it.id, ignoreCase = true) }
            .consumeOne {
                it.children.iterator().asSequence()
                    .consumeCharacterDescription()
                    .consumeCharacterPersonality()
                    .consumeCharacterHistory()
                    .consumeCharacterMotivation()
                    .consumeCharacterMiscItems()
                    .consumeCharacterVoice()
            }

        return characterBuilder.build()
    }

    private fun Sequence<Element>.consumeCharacterHeader() = consumeOne { node ->
        val charHeaderRegex = Regex("""([^,]*), ([\w]*) ([\w-]*).*?href="([^"]*)">""")
        val matchResult = charHeaderRegex.matchOrThrow(node.innerHTML, "Character header.")
        val (name, gender, race, url) = matchResult.destructured
        characterBuilder.name = name
        characterBuilder.gender = gender
        characterBuilder.race = race
        characterBuilder.url = "https://kassoon.com$url"
        Notice("Parsing character $name")
    }

    private fun Sequence<Element>.consumeCharacterDescription() = consumeOne { node ->
        console.log("Consuming char description ${node.id}")
        characterBuilder.description = node.textContent
            .handle("Character description.")
            .removePrefix("Description: ").trim()
    }

    private fun Sequence<Element>.consumeCharacterPersonality() = consumeOne { node ->
        console.log("Consuming char personality ${node.id}")
        characterBuilder.personality = node.textContent
            .handle("Character personality.")
            .removePrefix("Personality: ").trim()
    }

    private fun Sequence<Element>.consumeCharacterHistory() = consumeOne { node ->
        console.log("Consuming char history ${node.id}")
        characterBuilder.history = node.textContent
            .handle("Character history.")
            .removePrefix("History: ").trim()
    }

    private fun Sequence<Element>.consumeCharacterMotivation() = consumeOne { node ->
        console.log("Consuming char motivation ${node.id}")
        characterBuilder.motivation = node.textContent
            .handle("Character motivation.")
            .removePrefix("Motivation: ").trim()
    }

    private fun Sequence<Element>.consumeCharacterVoice() = consumeOne { node ->
        console.log("Consuming char motivation ${node.id}")
        characterBuilder.motivation = node.textContent
            .handle("Character motivation.")
            .removePrefix("Motivation: ").trim()
    }

    private fun Sequence<Element>.consumeCharacterMiscItems() = consumeOne { node ->
        console.log("Consuming char misc (occ) ${node.id}")
        val text = node.textContent.handle("Char misc content")
        val idealsRegex = Regex("""Ideals: ([^.]*)""")
        val ideals = idealsRegex.find(text)?.groupValues?.get(1)
        val flawsRegex = Regex("""Flaws: ([^.]*)""")
        val flaws = flawsRegex.find(text)?.groupValues?.get(1)
        val bondsRegex = Regex("""Bonds: ([^.]*)""")
        val bonds = bondsRegex.find(text)?.groupValues?.get(1)
        val occupationRegex = Regex("""Occupation: ([^.]*)""")
        val occupation = occupationRegex.matchOrThrow(text, "Character ideals").groupValues[1]
        characterBuilder.ideals = ideals?.trim()
        characterBuilder.flaws = flaws?.trim()
        characterBuilder.bonds = bonds?.trim()
        characterBuilder.occupation = occupation.trim()
    }
}

suspend fun detectAndParseKassoonCharactersFromHTML(html: String): List<Character> {
    val characterRegex = Regex("""["'](/\?page=dnd&amp;subpage=npc-generator&amp;[^"']*)["']""")
    val allMatches = characterRegex.findAll(html)
    console.log("Found ${allMatches.count()} characters")
    return allMatches.asFlow()
        .map {
            val url = it.groupValues[1].unescapeHTML()

            val characterDocument = WebsiteLoader().loadKassoonWebsite(url)
            KassoonCharacterParser(characterDocument).parseKassoonCharacter()
        }
        .toList()
}
