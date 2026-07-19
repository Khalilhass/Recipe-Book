package com.example.recipe_book.data.mapper

import com.example.recipe_book.data.remote.dto.UserDto
import com.example.recipe_book.domain.model.User
import com.google.firebase.Timestamp
import java.util.Date

fun UserDto.toDomain(id: String): User = User(
    id = id,
    name = name,
    email = email,
    photoUrl = photoUrl,
    country = country
)

fun User.toDto(): UserDto = UserDto(
    name = name,
    email = email,
    photoUrl = photoUrl,
    country = country,
    createdAt = Timestamp.now()
)