package models

data class Character(
    val name: String,
    val race: String,
    val gender: String,
    val url: String,
    val occupation: String,
    val voice: String,
    val ideals: String?,
    val flaws: String?,
    val bonds: String?,
    val description: String,
    val personality: String,
    val history: String,
    val motivation: String
)
