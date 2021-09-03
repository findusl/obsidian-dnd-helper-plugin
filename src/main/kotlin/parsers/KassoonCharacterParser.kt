package parsers

import models.Character
import dependencies.Notice
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.ParentNode
import util.*

class KassoonCharacterParser(private val document: Document, private val logger: StepAwareLogger): AbstractCharacterParser() {

    private val characterBuilder = CharacterBuilder()

    fun parseKassoonCharacter(): Character {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE") // why would parentNode not be a ParentNode?
        val contentNode = document.getElementById("npcTitle")?.parentNode as? ParentNode
            ?: logger.logAndThrow("Cannot find content node")

        val nodes = contentNode.children
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
        val matchResult = charHeaderRegex.find(node.innerHTML)
        if (matchResult == null) {
            logger.logError("Could not match header")
            return@consumeOne
        }
        val (name, gender, race, url) = matchResult.destructured
        characterBuilder.name = name
        characterBuilder.gender = gender
        characterBuilder.race = race
        characterBuilder.url = "https://kassoon.com$url"
        Notice("Parsing character $name")
    }

    private fun Sequence<Element>.consumeCharacterDescription() = consumeOne { node ->
        characterBuilder.description = node.textContent
            .defaultStringFallback("description textContent")
            .removePrefix("Description:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterPersonality() = consumeOne { node ->
        characterBuilder.personality = node.textContent
            .defaultStringFallback("personality textContent")
            .removePrefix("Personality:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterHistory() = consumeOne { node ->
        characterBuilder.history = node.textContent
            .defaultStringFallback("history textContent")
            .removePrefix("History:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterMotivation() = consumeOne { node ->
        characterBuilder.motivation = node.textContent
            .defaultStringFallback("motivation textContent")
            .removePrefix("Motivation:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterVoice() = consumeOne { node ->
        characterBuilder.voice = node.textContent
            .defaultStringFallback("motivation textContent")
            .removePrefix("Voice:").trim().cleanHtmlText()
    }

    private fun Sequence<Element>.consumeCharacterMiscItems() = consumeOne { node ->
        val text = node.textContent.defaultStringFallback("Misc items textContent")
        val idealsRegex = Regex("""Ideals: ([^.]*)""")
        val ideals = idealsRegex.find(text)?.groupValues?.get(1)
        val flawsRegex = Regex("""Flaws: ([^.]*)""")
        val flaws = flawsRegex.find(text)?.groupValues?.get(1)
        val bondsRegex = Regex("""Bonds: ([^.]*)""")
        val bonds = bondsRegex.find(text)?.groupValues?.get(1)
        val occupationRegex = Regex("""Occupation: ([^.]*)""")
        val occupation = occupationRegex.find(text)?.groupValues?.get(1)
        characterBuilder.ideals = ideals?.trim()
        characterBuilder.flaws = flaws?.trim()
        characterBuilder.bonds = bonds?.trim()
        characterBuilder.occupation = occupation?.trim().defaultStringFallback("occupation")
    }

    private fun String?.defaultStringFallback(variableName: String): String {
        return logger.logIfNullAndDefaultFallback(this, variableName)
    }
}

suspend fun detectAndParseKassoonCharactersFromHTML(html: String, parentLogger: StepAwareLogger? = null): List<Character> {
    val characterRegexWithName =
        Regex("""([^.>]*)<a[^>]*?href=["'](/\?page=dnd&amp;subpage=npc-generator&amp;[^"']*)["']>([^<]*)""")
    val allMatches = characterRegexWithName.findAll(html)
    console.log("Found ${allMatches.count()} characters")
    return allMatches.asFlow()
        .map {
            val (preLinkText, urlWithEscapeCharacters, linkText) = it.destructured
            val url = urlWithEscapeCharacters.unescapeHTML()
            if (url == null) {
                parentLogger?.logError("Could not parse url in character ${it.value}")
                return@map null
            }

            val logger = StepAwareLogger("Character $url", parentLogger)
            try {
                val characterDocument = WebsiteLoader().loadRelativeWebsite(url)
                var character = KassoonCharacterParser(characterDocument, logger).parseKassoonCharacter()

                character = tryFixName(linkText, preLinkText, character)

                character = tryFixOccupation(linkText, preLinkText, character)
                character
            } catch (e: AlreadyLoggedException) {
                null
            } catch (e: Exception) {
                logger.logError("Unexpected Exception $e")
                null
            }
        }
        .filterNotNull()
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
