package parsers

import util.SERIALIZATION_ERROR_PLACEHOLDER
import models.Service

@Suppress("MemberVisibilityCanBePrivate", "unused") // unused so far
abstract class AbstractServiceParser {

    protected class ServiceBuilder {
        var name: String = SERIALIZATION_ERROR_PLACEHOLDER
        var type: String = SERIALIZATION_ERROR_PLACEHOLDER
        var owner: String = SERIALIZATION_ERROR_PLACEHOLDER
        var locationDescription: String = SERIALIZATION_ERROR_PLACEHOLDER
        var description: String = SERIALIZATION_ERROR_PLACEHOLDER
        var mapLink: String = SERIALIZATION_ERROR_PLACEHOLDER
        var specials: List<String> = listOf()
        var patrons: List<String> = listOf()

        fun build(): Service {
            // throws exceptions if not all values were set
            return Service(
                name = name,
                type = type,
                owner = owner,
                locationDescription = locationDescription,
                description = description,
                mapLink = mapLink,
                specials = specials,
                patrons = patrons
            )
        }
    }
}