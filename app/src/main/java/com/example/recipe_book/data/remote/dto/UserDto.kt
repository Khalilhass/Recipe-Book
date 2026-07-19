package com.example.recipe_book.data.remote.dto

import com.google.firebase.Timestamp

data class UserDto(
    var name: String = "",
    var email: String = "",
    var photoUrl: String = "",
    var country: String = "",
    var createdAt: Timestamp? = null
)