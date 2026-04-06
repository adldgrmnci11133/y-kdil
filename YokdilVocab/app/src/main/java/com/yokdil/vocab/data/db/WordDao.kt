package com.yokdil.vocab.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yokdil.vocab.data.db.entities.UserProgress
import com.yokdil.vocab.data.db.entities.Word

@Dao
interface WordDao {

    // ---- Words ----

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWords(): LiveData<List<Word>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Int): Word?

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int): List<Word>

    @Query("SELECT * FROM words WHERE is_favorite = 1 ORDER BY word ASC")
    fun getFavoriteWords(): LiveData<List<Word>>

    @Query("SELECT * FROM words WHERE is_learned = 0 ORDER BY RANDOM()")
    fun getUnlearnedWords(): LiveData<List<Word>>

    @Query("SELECT COUNT(*) FROM words")
    fun getTotalWordCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM words WHERE is_learned = 1")
    fun getLearnedWordCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM words WHERE is_favorite = 1")
    fun getFavoriteWordCount(): LiveData<Int>

    @Query("SELECT * FROM words WHERE is_learned = 0 AND (:category = 'all' OR category = :category) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getWordsForFlashcard(limit: Int, category: String = "all"): List<Word>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): Word?

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR turkish_meaning LIKE '%' || :query || '%'")
    fun searchWords(query: String): LiveData<List<Word>>

    @Query("UPDATE words SET is_favorite = :isFavorite WHERE id = :wordId")
    suspend fun updateFavorite(wordId: Int, isFavorite: Boolean)

    @Query("UPDATE words SET is_learned = :isLearned WHERE id = :wordId")
    suspend fun updateLearned(wordId: Int, isLearned: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    // ---- Progress ----

    @Query("SELECT * FROM user_progress WHERE word_id = :wordId")
    suspend fun getProgress(wordId: Int): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserProgress)

    @Query("SELECT SUM(correct_count) FROM user_progress")
    fun getTotalCorrect(): LiveData<Int>

    @Query("SELECT SUM(wrong_count) FROM user_progress")
    fun getTotalWrong(): LiveData<Int>

    @Query("SELECT * FROM user_progress ORDER BY last_seen DESC LIMIT 7")
    fun getRecentProgress(): LiveData<List<UserProgress>>
}
