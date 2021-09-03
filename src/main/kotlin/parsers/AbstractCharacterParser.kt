package parsers

import SERIALIZATION_ERROR_PLACEHOLDER
import models.Character

abstract class AbstractCharacterParser {

    protected class CharacterBuilder {
        var name: String = SERIALIZATION_ERROR_PLACEHOLDER
        var race: String = SERIALIZATION_ERROR_PLACEHOLDER
        var gender: String = SERIALIZATION_ERROR_PLACEHOLDER
        var url: String = SERIALIZATION_ERROR_PLACEHOLDER
        var occupation: String = SERIALIZATION_ERROR_PLACEHOLDER
        var voice: String = SERIALIZATION_ERROR_PLACEHOLDER
        var ideals: String? = null
        var flaws: String? = null
        var bonds: String? = null
        var description: String = SERIALIZATION_ERROR_PLACEHOLDER
        var personality: String = SERIALIZATION_ERROR_PLACEHOLDER
        var history: String = SERIALIZATION_ERROR_PLACEHOLDER
        var motivation: String = SERIALIZATION_ERROR_PLACEHOLDER


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