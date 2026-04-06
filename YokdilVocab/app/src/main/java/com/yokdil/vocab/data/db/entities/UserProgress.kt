package com.yokdil.vocab.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_progress",
    foreignKeys = [ForeignKey(
        entity = Word::class,
        parentColumns = ["id"],
        childColumns = ["word_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UserProgress(
    @PrimaryKey
    @ColumnInfo(name = "word_id")
    val wordId: Int,
    @ColumnInfo(name = "correct_count")
    val correctCount: Int = 0,
    @ColumnInfo(name = "wrong_count")
    val wrongCount: Int = 0,
    @ColumnInfo(name = "last_seen")
    val lastSeen: Long = 0L,
    @ColumnInfo(name = "next_review")
    val nextReview: Long = 0L
)
