package com.yokdil.vocab.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    @ColumnInfo(name = "turkish_meaning")
    val turkishMeaning: String,
    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,
    val synonyms: String,      // JSON array string
    val antonyms: String,      // JSON array string
    @ColumnInfo(name = "example_sentence")
    val exampleSentence: String,
    @ColumnInfo(name = "example_translation")
    val exampleTranslation: String,
    val category: String = "fen",
    val difficulty: Int = 2,   // 2=orta, 3=ileri
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_learned")
    val isLearned: Boolean = false,
    @ColumnInfo(name = "exam_year")
    val examYear: String = ""
)
