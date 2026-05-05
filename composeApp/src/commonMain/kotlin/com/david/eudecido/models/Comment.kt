package com.david.eudecido.models

data class Comment(
    val id: String,
    val author: String,
    val text: String,
    val likes: Int
)
