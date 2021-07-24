package models

data class Service(
    val name: String,
    val type: String,
    val owner: String,
    val locationDescription: String?,
    val description: String,
    val mapLink: String,
    val specials: List<String>?,
    val patrons: List<String>?
)
