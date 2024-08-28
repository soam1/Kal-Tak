package com.akashsoam.kaltak.models

data class Source(
    val id: String,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Source

        if (id != other.id) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode()
            ?: 0 // Use safe call operator and Elvis operator to handle null values
    }
}