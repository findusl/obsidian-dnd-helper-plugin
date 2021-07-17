package parsers

import Service

abstract class AbstractServiceParser {

    protected class ServiceBuilder {
        lateinit var name: String
        lateinit var type: String
        lateinit var owner: String
        lateinit var locationDescription: String
        lateinit var description: String
        lateinit var mapLink: String
        lateinit var specials: List<String>
        lateinit var patrons: List<String>

        fun build(): Service {
            // throws exceptions if not all values were set
            return Service(name, type, owner, locationDescription, description, mapLink, specials, patrons)
        }
    }
}