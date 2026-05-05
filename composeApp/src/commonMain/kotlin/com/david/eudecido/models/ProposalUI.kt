package com.david.eudecido.models

data class ProposalUI(
    val id: String,
    val title: String,
    val summary: String,
    val votes: Int,
    val approval: Int
)
