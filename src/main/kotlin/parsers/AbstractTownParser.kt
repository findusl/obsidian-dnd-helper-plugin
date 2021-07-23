package parsers

import Character
import Service
import Town

abstract class AbstractTownParser {

    protected class TownBuilder {
        lateinit var name: String
        lateinit var type: String
        lateinit var url: String
        lateinit var population: String
        lateinit var size: String
        lateinit var demographics: String
        lateinit var wealth: String
        lateinit var description: String
        lateinit var defenses: String
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