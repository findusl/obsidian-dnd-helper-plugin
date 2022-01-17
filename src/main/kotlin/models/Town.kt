package models

data class Town(
    val name: String,
    val type: String,
    val url: String,
    val population: String,
    val size: String,
    val demographics: String,
    val wealth: String,
    val description: String,
    val defenses: String,
    val organizations: List<String>,
    val services: List<Service>,
    val characters: List<Character>
)
