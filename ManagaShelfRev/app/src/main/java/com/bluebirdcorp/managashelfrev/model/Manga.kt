package com.bluebirdcorp.managashelfrev.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "manga_table")
data class Manga(
    @PrimaryKey val id: String,
    val image: String?,
    val score: Double?,
    val title: String?,
    val popularity: Int?,
    val publishedChapterDate: Long?,
    val category: String?,
    var isLiked: Boolean
)
