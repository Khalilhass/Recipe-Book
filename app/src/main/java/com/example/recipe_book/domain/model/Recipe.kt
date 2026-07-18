package com.example.recipe_book.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val category: String,
    val imageUrl: String,
    val videoUrl: String?,
    val authorId: String,
    val authorName: String,
    val createdAt: Long
)
