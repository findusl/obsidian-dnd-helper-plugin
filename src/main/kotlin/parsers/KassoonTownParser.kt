package parsers

import models.Town
import org.w3c.dom.Document
import org.w3c.dom.Element
import util.*

class KassoonTownParser(private val document: Document, private val logger: StepAwareLogger) : AbstractTownParser() {

    private val townBuilder = TownBuilder()
    private val servicesParser = KassoonServicesParser(logger)

    suspend fun parseKassoonTown(): Town {
        val contentNode = document.getElementById("content") ?: logger.logAndThrow("Missing content node.")
        val nodes = contentNode.children
        nodes.iterator().asSequence()
            .dropWhile { !it.isH2() }
            .consumeTownHeader()
            .dropWhile { !it.isParagraph() }
            // Stats section
            .consumeStats()
            .dropWhile { !"townDesc".equals(it.id, ignoreCase = true) }
            .consumeDescription()
            .consumeDefenses()
            // shop index
            .consumeOne { node ->
                console.log("Consuming Shop names")
                servicesParser.extractServiceNames(node)
            }

        val contentHtml = contentNode.innerHTML

        val shops = servicesParser.extractServicesFromHTML(contentHtml)
        townBuilder.services.addAll(shops)

        townBuilder.characters.addAll(detectAndParseKassoonCharactersFromHTML(contentHtml, logger))

        val result = townBuilder.build()
        console.log("Result of Town parsing: $result")
        return result
    }

    private fun Sequence<Element>.consumeTownHeader() = consumeOne { node ->
        val townHeaderRegex = Regex("""([^,]*), ([\w\s]*) .*? href="([^"]*)">""")
        val matchResult = townHeaderRegex.find(node.innerHTML)
        if (matchResult == null) {
            logger.logError("Could not match town header")
            return@consumeOne
        }
        val (name, type, url) = matchResult.destructured
        townBuilder.name = name
        townBuilder.type = type
        townBuilder.url = "https://kassoon.com$url"
        console.log("Parsing town header $name")
    }

    private fun Sequence<Element>.consumeStats() = consumeOne { node ->
        val statsRegex = Regex("""Population: (.*?), Size: (.*)[\s\S]*?Demographics: (.*)[\s\S]*?Wealth: ([^.]*)""")
        val textContent = logger.logIfNull(node.textContent, "Stats") ?: return@consumeOne
        val matchResult = statsRegex.find(textContent.substring(1))
        if (matchResult == null) {
            logger.logError("Could not match stats in $textContent")
            return@consumeOne
        }
        val (population, size, demographics, wealth) = matchResult.destructured
        townBuilder.population = population
        townBuilder.size = size
        townBuilder.demographics = demographics
        townBuilder.wealth = wealth
        console.log("Parse stats wealth $wealth")
    }

    private fun Sequence<Element>.consumeDescription() = consumeOne { node ->
        console.log("Consuming Town description")
        val description = node.textContent?.cleanHtmlText()
        townBuilder.description = logger.logIfNullAndDefaultFallback(description, "Description textContent")
    }

    private fun Sequence<Element>.consumeDefenses() = consumeOne { node ->
        console.log("Consuming Town defense")
        val defenses = node.textContent?.cleanHtmlText()?.removePrefix("Defenses: ")
        townBuilder.defenses = logger.logIfNullAndDefaultFallback(defenses, "Defenses textContent")
    }
}
