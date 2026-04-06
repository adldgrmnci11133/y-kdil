package com.yokdil.vocab.data.repository

import androidx.lifecycle.LiveData
import com.yokdil.vocab.data.db.WordDao
import com.yokdil.vocab.data.db.entities.UserProgress
import com.yokdil.vocab.data.db.entities.Word

class WordRepository(private val wordDao: WordDao) {

    val allWords: LiveData<List<Word>> = wordDao.getAllWords()
    val favoriteWords: LiveData<List<Word>> = wordDao.getFavoriteWords()
    val unlearnedWords: LiveData<List<Word>> = wordDao.getUnlearnedWords()
    val totalWordCount: LiveData<Int> = wordDao.getTotalWordCount()
    val learnedWordCount: LiveData<Int> = wordDao.getLearnedWordCount()
    val favoriteWordCount: LiveData<Int> = wordDao.getFavoriteWordCount()
    val totalCorrect: LiveData<Int> = wordDao.getTotalCorrect()
    val totalWrong: LiveData<Int> = wordDao.getTotalWrong()

    suspend fun getWordById(id: Int): Word? = wordDao.getWordById(id)

    suspend fun getRandomWords(limit: Int) = wordDao.getRandomWords(limit)

    suspend fun getWordsForFlashcard(limit: Int = 20, category: String = "all") =
        wordDao.getWordsForFlashcard(limit, category)

    suspend fun getRandomWord(): Word? = wordDao.getRandomWord()

    fun searchWords(query: String): LiveData<List<Word>> = wordDao.searchWords(query)

    suspend fun toggleFavorite(wordId: Int, isFavorite: Boolean) =
        wordDao.updateFavorite(wordId, isFavorite)

    suspend fun markLearned(wordId: Int, isLearned: Boolean) =
        wordDao.updateLearned(wordId, isLearned)

    suspend fun recordAnswer(wordId: Int, isCorrect: Boolean) {
        val existing = wordDao.getProgress(wordId)
        val now = System.currentTimeMillis()
        val nextReview = if (isCorrect) now + 24 * 60 * 60 * 1000L else now + 4 * 60 * 60 * 1000L
        val progress = existing?.copy(
            correctCount = if (isCorrect) existing.correctCount + 1 else existing.correctCount,
            wrongCount = if (!isCorrect) existing.wrongCount + 1 else existing.wrongCount,
            lastSeen = now,
            nextReview = nextReview
        ) ?: UserProgress(
            wordId = wordId,
            correctCount = if (isCorrect) 1 else 0,
            wrongCount = if (!isCorrect) 1 else 0,
            lastSeen = now,
            nextReview = nextReview
        )
        wordDao.upsertProgress(progress)
    }

    fun getRecentProgress(): LiveData<List<UserProgress>> = wordDao.getRecentProgress()
}
