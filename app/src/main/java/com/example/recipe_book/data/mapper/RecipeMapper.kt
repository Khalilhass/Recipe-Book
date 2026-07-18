package com.example.recipe_book.data.mapper

import com.example.recipe_book.data.remote.dto.RecipeDto
import com.example.recipe_book.domain.model.Recipe
import com.google.firebase.Timestamp
import java.util.Date

fun RecipeDto.toDomain(id: String): Recipe = Recipe(
    id = id,
    title = title,
    ingredients = ingredients,
    steps = steps,
    category = category,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    authorId = authorId,
    authorName = authorName,
    createdAt = createdAt?.toDate()?.time ?: 0L
)

fun Recipe.toDto(): RecipeDto = RecipeDto(
    title = title,
    titleLower = title.lowercase(),
    ingredients = ingredients,
    steps = steps,
    category = category,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    authorId = authorId,
    authorName = authorName,
    createdAt = Timestamp(Date(createdAt))
)