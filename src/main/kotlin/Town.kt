import org.w3c.dom.Document
import org.w3c.dom.Element

data class Town(
    val name: String,
    val type: String,
    val url: String,
    val population: String,
    val size: String,
    val demographics: String,
    val wealth: String
)

internal class TownBuilder {
    lateinit var name: String
    lateinit var type: String
    lateinit var url: String
    lateinit var population: String
    lateinit var size: String
    lateinit var demographics: String
    lateinit var wealth: String

    fun build(): Town {
        // throws exceptions if not all values were set
        return Town(name, type, url, population, size, demographics, wealth)
    }
}

fun Document.parseKassoonTown(): Town {
    val nodes = getElementById("content")?.children ?: throw NoSuchElementException("Missing content.")
    println("There are ${nodes.length} nodes")
    val townBuilder = TownBuilder()
    nodes.iterator().asSequence()
        .dropWhile { !it.isH2() }
        // Town Header section
        .consumeOne { node ->
            val townHeaderRegex = Regex("""([\w\s]*), ([\w\s]*) .* href="(.*)">.*""")
            val matchResult = townHeaderRegex.matchOrThrow(node.innerHTML, "Town header")
            val (name, type, url) = matchResult.destructured
            townBuilder.name = name
            townBuilder.type = type
            townBuilder.url = "https://kassoon.com$url"
            Notice("Parsing town $name")
        }
        .dropWhile { !it.isParagraph() }
        // Stats section
        .consumeOne { node ->
            val statsRegex = Regex("""Population: (.*), Size: (.*)\nDemographics: (.*)[\w\W]*Wealth: ([^.]*)""")
            val matchResult = statsRegex.matchOrThrow(node.textContent!!.substring(1), "Stats")
            val (population, size, demographics, wealth) = matchResult.destructured
            townBuilder.population = population
            townBuilder.size = size
            townBuilder.demographics = demographics
            townBuilder.wealth = wealth
            Notice("Parse wealth $wealth")
        }
        .dropWhile { !"townDesc".equals(it.id, ignoreCase = true) }
        // General Town Description

    val result = townBuilder.build()
    Notice("Result: $result")
    return result
}

private fun Element.isH2(): Boolean = "H2".equals(tagName, ignoreCase = true)
private fun Element.isParagraph(): Boolean = "P".equals(tagName, ignoreCase = true)

private fun Regex.matchOrThrow(input: CharSequence, debugInfo: String): MatchResult {
    return find(input) ?: throw IllegalArgumentException("$debugInfo: Could not match $input")
}
