package com.example.recipe_book.data.remote.dto


import com.google.firebase.Timestamp

data class RecipeDto(
    var title: String = "",
    var titleLower: String = "",
    var ingredients: List<String> = emptyList(),
    var steps: List<String> = emptyList(),
    var category: String = "",
    var imageUrl: String = "",
    var videoUrl: String? = null,
    var authorId: String = "",
    var authorName: String = "",
    var createdAt: Timestamp? = null
)