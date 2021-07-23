package parsers

import Character

abstract class AbstractCharacterParser {

    protected class CharacterBuilder {
        lateinit var name: String
        lateinit var race: String
        lateinit var gender: String
        lateinit var url: String
        lateinit var occupation: String
        lateinit var voice: String
        var ideals: String? = null
        var flaws: String? = null
        var bonds: String? = null
        lateinit var description: String
        lateinit var personality: String
        lateinit var history: String
        lateinit var motivation: String


        fun build(): Character {
            // throws exceptions if not all values were set
            return Character(
                name = name,
                race = race,
                gender = gender,
                url = url,
                occupation = occupation,
                voice = voice,
                ideals = ideals,
                flaws = flaws,
                bonds = bonds,
                description = description,
                personality = personality,
                history = history,
                motivation = motivation
            )
        }
    }
}