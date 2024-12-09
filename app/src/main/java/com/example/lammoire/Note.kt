package com.example.lammoire

data class Note(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val location: String = "",
    val creationDate: Long = 0L
)