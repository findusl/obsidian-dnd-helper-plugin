package parsers

import models.Service
import util.camelCaseToCapitalizedSentenceCase
import util.cleanHtmlText
import util.removeLineBreaks
import org.w3c.dom.Element
import util.StepAwareLogger

class KassoonServicesParser(private val townLogger: StepAwareLogger) {
    private var serviceNames = mutableListOf<String>()

    fun extractServiceNames(serviceIndexParagraph: Element) {
        console.log("Extracting shop names from ${serviceIndexParagraph.innerHTML}")
        val serviceLinkRegex = Regex("""<a[^h]*href="#(\w*)">""")
        serviceLinkRegex.findAll(serviceIndexParagraph.innerHTML)
            .map {
                it.groupValues[1]
            }.toCollection(serviceNames)
        if (serviceNames.isEmpty()) {
            townLogger.logError("No service names were found")
        }
        console.log("Extracted shopNames $serviceNames")
    }

    fun extractServicesFromHTML(html: String): List<Service> {
        if (serviceNames.isEmpty()) {
            townLogger.logError("Shop names were not yet parsed")
            return listOf()
        }
        // consider starting to match from index of <h2>Shops</h2>. then maybe continue to match from the previous match,
        // as the index should have been in order.

        val shops = mutableListOf<Service>()
        for (serviceId in serviceNames) {
            var service = tryMatchShop(serviceId, html)
            if (service == null) {
                service = tryMatchHousing(serviceId, html)
                if (service == null) {
                    StepAwareLogger("serviceId", townLogger).logError("Could not match service.")
                    continue
                }
            }

            shops.add(service)
        }
        return shops.distinctBy { it.name }
    }
}

private fun tryMatchShop(shopId: String, contentHtml: String): Service? {
    val shopRegex = shopRegex(shopId)
    val match = shopRegex.find(contentHtml) ?: return null
    val (type, name, owner, location, description, mapLink, specialsList, patronsList) = match.destructured
    val specialMatches = liItemRegex.findAll(specialsList)
    val specials = specialMatches.map { it.groupValues[1].cleanHtmlText() }.toList()
    if (specials.count() != 3) {
        console.log("Usually there are 3 specials, something might be wrong $specialsList")
    }
    val patronMatches = liItemRegex.findAll(patronsList)
    val patrons = patronMatches.map { it.groupValues[1].cleanHtmlText() }.toList()
    val completeMapLink = mapLink.completeMapLink()

    console.log("Extracted shop $name")
    return Service(
        name.cleanHtmlText(), type, owner.cleanHtmlText(), location.cleanHtmlText(),
        description.cleanHtmlText(), completeMapLink, specials, patrons
    )
}

/**
 * Resulting Regex matches the following groups in this order:
 * - Type (e.g. Tavern)
 * - Name (e.g. Isabella's Inn)
 * - Owner (e.g. Isabella Stanford)
 * - Location
 * - Description
 * - Map link
 * - List of specials as list of <li></li>
 * - List of other patrons as list of <li></li>
 */
private fun shopRegex(shopId: String): Regex = Regex("""
<p id="$shopId"><strong>([^:]*): ([^<]*)</strong><br ?/?>[^<]*
<u>Owner:</u> ([^,]*),[^<]*<[^>]*>\[Details\]</a>[^<]*
</p>[^<]*
<u>Location:</u> ([^>]*)<br ?/?>[^<]*
<u>Description:</u> ([^>]*)<br ?/?>[^<]*
(?:<a target="_blank" href="[^"]*">Quests and Rumors</a> |)?[^<]*
<a target="_blank" href="([^"]*)">Map</a>[^<]*
<ul style="margin:0;"><u>Specials:</u>([\s\S]*?)
</ul><br ?/?>[^<]*
<ul style="margin:0;"><u>Other Patrons:</u>([\s\S]*?)
</ul>
""".trimIndent().removeLineBreaks())

private fun tryMatchHousing(housingId: String, contentHtml: String): Service? {
    val housingRegex = housingRegex(housingId)
    val match = housingRegex.find(contentHtml) ?: return null
    val (name, owner, mapLink, description) = match.destructured
    val completeMapLink = mapLink.completeMapLink()
    val type = housingId.camelCaseToCapitalizedSentenceCase().removePrefix("Town ")

    console.log("Extracted housing $name")
    return Service(
        name, type, owner.cleanHtmlText(), null,
        description.cleanHtmlText(), completeMapLink, null, null
    )
}

/**
 * Resulting Regex matches the following groups in this order:
 * - Name (e.g. Small Cottage or Cruck house)
 * - Owner (e.g. Isabella Stanford)
 * - Map link
 * - Description
 */
private fun housingRegex(housingId: String): Regex = Regex("""
<p id="$housingId"><strong>([^<]*)</strong><br ?/?>[^<]*
<u>Owner:</u> ([^,]*),[^<]*<[^>]*>\[Details\]</a>[^<]*
<br ?/?>[^<]*<a target="_blank" href="([^"]*)">Map</a>[^>]*>
[^>]*>([^<]*)
""".trimIndent().removeLineBreaks())

private fun String.completeMapLink() = "https://kassoon.com$this"

/**
 * Patrons include a link which I want to avoid, specials do not have such a link.
 * This regex works for specials and patrons.
 */
private val liItemRegex = Regex("""
    <li>([^<]*)<
""".trimIndent())
