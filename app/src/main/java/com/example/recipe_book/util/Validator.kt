package com.example.recipe_book.util


object Validators {

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && EMAIL_REGEX.matches(email.trim())

    fun isValidPassword(password: String): Boolean =
        password.length >= 6

    fun isValidName(name: String): Boolean =
        name.trim().length in 2..50

    fun isValidRecipeTitle(title: String): Boolean =
        title.trim().length in 3..100

    fun isValidYoutubeUrl(url: String): Boolean {
        if (url.isBlank()) return true // optional field
        val pattern = Regex(
            "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]+.*$",
            RegexOption.IGNORE_CASE
        )
        return pattern.matches(url.trim())
    }

    /** Splits comma-separated free text into a trimmed, non-empty list. */
    fun splitCommaSeparated(text: String): List<String> =
        text.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    /** Normalizes a free-text category to Title Case so "dessert" and "Dessert"
     *  are treated as the same tab (blueprint Section 1.3 edge case). */
    fun normalizeCategory(category: String): String =
        category.trim().lowercase().replaceFirstChar { it.uppercase() }
}