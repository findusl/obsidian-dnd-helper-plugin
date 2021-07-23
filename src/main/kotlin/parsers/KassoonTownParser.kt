package parsers

import Town
import org.w3c.dom.Document
import org.w3c.dom.Element
import util.*

class KassoonTownParser(private val document: Document) : AbstractTownParser() {

    private val townBuilder = TownBuilder()
    private val servicesParser = KassoonServicesParser()

    suspend fun parseKassoonTown(): Town {
        val contentNode = document.getElementById("content") ?: throw NoSuchElementException("Missing content.")
        val nodes = contentNode.children
        nodes.iterator().asSequence()
            .dropWhile { !it.isH2() }
            .consumeTownHeader()
            .dropWhile { !it.isParagraph() }
            // Stats section
            .consumeStats()
            .dropWhile { !"townDesc".equals(it.id, ignoreCase = true) }
            // General Town Description
            .consumeOne { node ->
                console.log("Consuming Town description")
                townBuilder.description = node.textContent.handle("Town Description")
                    .cleanHtmlText()
            }
            .consumeOne { node ->
                console.log("Consuming Town defense")
                townBuilder.defenses = node.textContent.handle("Town Defense")
                    .cleanHtmlText().removePrefix("Defenses: ")
            }
            // shop index
            .consumeOne { node ->
                console.log("Consuming Shop names")
                servicesParser.extractServiceNames(node)
            }

        val contentHtml = contentNode.innerHTML

        val shops = servicesParser.extractServicesFromHTML(contentHtml)
        townBuilder.services.addAll(shops)

        townBuilder.characters.addAll(detectAndParseKassoonCharactersFromHTML(contentHtml))

        val result = townBuilder.build()
        console.log("Result of Town parsing: $result")
        return result
    }

    private fun Sequence<Element>.consumeTownHeader() = consumeOne { node ->
        // TODO CHECK REGEX that lazy matching looks suspicious
        val townHeaderRegex = Regex("""([^,]*), ([\w\s]*?) .*? href="([^"]*)">""")
        val matchResult = townHeaderRegex.matchOrThrow(node.innerHTML, "Town header")
        val (name, type, url) = matchResult.destructured
        townBuilder.name = name
        townBuilder.type = type
        townBuilder.url = "https://kassoon.com$url"
        console.log("Parsing town $name")
    }

    private fun Sequence<Element>.consumeStats() = consumeOne { node ->
        val statsRegex = Regex("""Population: ([^,]*), Size: (.*)[\s\S]*?Demographics: (.*)[\s\S]*?Wealth: ([^.]*)""")
        val textContent = node.textContent.handle("Stats section")
        val matchResult = statsRegex.matchOrThrow(textContent.substring(1), "Stats")
        val (population, size, demographics, wealth) = matchResult.destructured
        townBuilder.population = population
        townBuilder.size = size
        townBuilder.demographics = demographics
        townBuilder.wealth = wealth
        console.log("Parse wealth $wealth")
    }
}
