package com.akashsoam.kaltak.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    //since source data type is not supported by our sqlite db, we will store it as string using type converter
    val source: Source,
    val title: String,
    val url: String,
    val urlToImage: Any
): Serializable