package parsers

import models.Character
import Notice
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.ParentNode
import util.*

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
        val matchResult = charHeaderRegex.matchOrThrow(node.innerHTML, "models.Character header.")
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
            .handle("models.Character description.")
            .removePrefix("Description:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterPersonality() = consumeOne { node ->
        console.log("Consuming char personality ${node.id}")
        characterBuilder.personality = node.textContent
            .handle("models.Character personality.")
            .removePrefix("Personality:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterHistory() = consumeOne { node ->
        console.log("Consuming char history ${node.id}")
        characterBuilder.history = node.textContent
            .handle("models.Character history.")
            .removePrefix("History:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterMotivation() = consumeOne { node ->
        console.log("Consuming char motivation ${node.id}")
        characterBuilder.motivation = node.textContent
            .handle("models.Character motivation.")
            .removePrefix("Motivation:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterVoice() = consumeOne { node ->
        console.log("Consuming char voice ${node.id}")
        characterBuilder.voice = node.textContent
            .handle("models.Character motivation.")
            .removePrefix("Voice:").trim().cleanHtmlText()
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
        val occupation = occupationRegex.matchOrThrow(text, "models.Character ideals").groupValues[1]
        characterBuilder.ideals = ideals?.trim()
        characterBuilder.flaws = flaws?.trim()
        characterBuilder.bonds = bonds?.trim()
        characterBuilder.occupation = occupation.trim()
    }
}

suspend fun detectAndParseKassoonCharactersFromHTML(html: String): List<Character> {
    val characterRegexWithName =
        Regex("""([^.>]*)<a[^>]*?href=["'](/\?page=dnd&amp;subpage=npc-generator&amp;[^"']*)["']>([^<]*)""")
    val allMatches = characterRegexWithName.findAll(html)
    console.log("Found ${allMatches.count()} characters")
    return allMatches.asFlow()
        .map {
            val (preLinkText, url, linkText) = it.destructured

            val characterDocument = WebsiteLoader().loadKassoonWebsite(url.unescapeHTML())
            var character = KassoonCharacterParser(characterDocument).parseKassoonCharacter()

            character = tryFixName(linkText, preLinkText, character)

            character = tryFixOccupation(linkText, preLinkText, character)
            character
        }
        .toList()
}

private fun tryFixName(
    linkText: String,
    preLinkText: String,
    character: Character
): Character {
    // The character name can differ between town and character page.
    // That breaks linking so I try to set the name fitting the town page.
    val hopefullyName = if (linkText.equals("[Details]", ignoreCase = true)) {
        preLinkText.trim().substringBefore(',')
    } else {
        linkText.trim().substringBefore(',')
    }
    console.log("Replacing ${character.name} with $hopefullyName in the hopes it is correct")

    return character.copy(name = hopefullyName)
}

private fun tryFixOccupation(
    linkText: String,
    preLinkText: String,
    character: Character
): Character {
    // Some characters have special occupations called out in text but actually not matching on character page
    var character1 = character
    if (!linkText.equals("[Details]", ignoreCase = true)) {
        val occupationRegex = Regex("""The (\w*)""")
        val occupationMatch = occupationRegex.find(preLinkText)
        if (occupationMatch != null) {
            val newOccupation = occupationMatch.groupValues[1].replaceFirstChar { it.uppercaseChar() } + " (inconsistent)"
            character1 = character1.copy(occupation = newOccupation)
        }
    }
    return character1
}
