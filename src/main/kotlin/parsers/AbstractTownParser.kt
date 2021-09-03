package parsers

import SERIALIZATION_ERROR_PLACEHOLDER
import models.Character
import models.Service
import models.Town

abstract class AbstractTownParser {

    protected class TownBuilder {
        var name: String = SERIALIZATION_ERROR_PLACEHOLDER
        var type: String = SERIALIZATION_ERROR_PLACEHOLDER
        var url: String = SERIALIZATION_ERROR_PLACEHOLDER
        var population: String = SERIALIZATION_ERROR_PLACEHOLDER
        var size: String = SERIALIZATION_ERROR_PLACEHOLDER
        var demographics: String = SERIALIZATION_ERROR_PLACEHOLDER
        var wealth: String = SERIALIZATION_ERROR_PLACEHOLDER
        var description: String = SERIALIZATION_ERROR_PLACEHOLDER
        var defenses: String = SERIALIZATION_ERROR_PLACEHOLDER
        val services: MutableList<Service> = mutableListOf()
        val characters: MutableList<Character> = mutableListOf()

        fun build(): Town {
            // throws exceptions if not all values were set
            return Town(
                name = name,
                type = type,
                url = url,
                population = population,
                size = size,
                demographics = demographics,
                wealth = wealth,
                description = description,
                defenses = defenses,
                services = services,
                characters = characters
            )
        }
    }
}