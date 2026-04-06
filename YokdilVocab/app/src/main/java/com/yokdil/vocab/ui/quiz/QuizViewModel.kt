package com.yokdil.vocab.ui.quiz

import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.data.repository.WordRepository
import kotlinx.coroutines.launch

data class QuizQuestion(
    val word: Word,
    val type: QuizType,
    val options: List<String>,
    val correctAnswer: String
)

enum class QuizType { MEANING, SYNONYM, ANTONYM }

data class QuizResult(val totalQuestions: Int, val correctAnswers: Int)

class QuizViewModel(private val repository: WordRepository) : ViewModel() {

    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    private val _questions = MutableLiveData<List<QuizQuestion>>()
    val questions: LiveData<List<QuizQuestion>> = _questions

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _selectedAnswer = MutableLiveData<String?>()
    val selectedAnswer: LiveData<String?> = _selectedAnswer

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _quizFinished = MutableLiveData(false)
    val quizFinished: LiveData<Boolean> = _quizFinished

    val currentQuestion: LiveData<QuizQuestion?> = MediatorLiveData<QuizQuestion?>().apply {
        fun update() {
            val list = _questions.value ?: return
            val idx = _currentIndex.value ?: 0
            value = if (idx < list.size) list[idx] else null
        }
        addSource(_questions) { update() }
        addSource(_currentIndex) { update() }
    }

    val progress: LiveData<Pair<Int, Int>> = MediatorLiveData<Pair<Int, Int>>().apply {
        fun update() {
            val total = _questions.value?.size ?: 0
            val current = (_currentIndex.value ?: 0) + 1
            value = Pair(current.coerceAtMost(total), total)
        }
        addSource(_questions) { update() }
        addSource(_currentIndex) { update() }
    }

    fun loadQuiz(count: Int = 10) {
        viewModelScope.launch {
            val allWords = repository.getRandomWords(count + 10)
            if (allWords.size < 4) return@launch
            val selected = allWords.take(count)
            val questions = selected.mapNotNull { word ->
                buildQuestion(word, allWords)
            }
            _questions.value = questions
            _currentIndex.value = 0
            _score.value = 0
            _quizFinished.value = false
            _selectedAnswer.value = null
        }
    }

    private fun buildQuestion(word: Word, pool: List<Word>): QuizQuestion? {
        val synonyms: List<String> = try { gson.fromJson(word.synonyms, listType) } catch (e: Exception) { emptyList() }
        val antonyms: List<String> = try { gson.fromJson(word.antonyms, listType) } catch (e: Exception) { emptyList() }

        val availableTypes = mutableListOf(QuizType.MEANING)
        if (synonyms.isNotEmpty()) availableTypes.add(QuizType.SYNONYM)
        if (antonyms.isNotEmpty()) availableTypes.add(QuizType.ANTONYM)

        val type = availableTypes.random()
        val correct: String
        val wrongPool: List<String>

        when (type) {
            QuizType.MEANING -> {
                correct = word.turkishMeaning
                wrongPool = pool.filter { it.id != word.id }.map { it.turkishMeaning }.distinct()
            }
            QuizType.SYNONYM -> {
                correct = synonyms.first()
                wrongPool = pool.filter { it.id != word.id }
                    .flatMap { w -> try { gson.fromJson<List<String>>(w.synonyms, listType) } catch (e: Exception) { emptyList() } }
                    .distinct().filter { it != correct }
            }
            QuizType.ANTONYM -> {
                correct = antonyms.first()
                wrongPool = pool.filter { it.id != word.id }
                    .flatMap { w -> try { gson.fromJson<List<String>>(w.antonyms, listType) } catch (e: Exception) { emptyList() } }
                    .distinct().filter { it != correct }
            }
        }

        if (wrongPool.size < 3) return null

        val options = (wrongPool.shuffled().take(3) + correct).shuffled()
        return QuizQuestion(word, type, options, correct)
    }

    fun selectAnswer(answer: String) {
        if (_selectedAnswer.value != null) return
        _selectedAnswer.value = answer
        val isCorrect = answer == currentQuestion.value?.correctAnswer
        if (isCorrect) _score.value = (_score.value ?: 0) + 1
        viewModelScope.launch {
            currentQuestion.value?.word?.let { word ->
                repository.recordAnswer(word.id, isCorrect)
            }
        }
    }

    fun nextQuestion() {
        _selectedAnswer.value = null
        val idx = _currentIndex.value ?: 0
        val total = _questions.value?.size ?: 0
        if (idx + 1 >= total) {
            _quizFinished.value = true
        } else {
            _currentIndex.value = idx + 1
        }
    }

    fun getResult(): QuizResult {
        val total = _questions.value?.size ?: 0
        val correct = _score.value ?: 0
        return QuizResult(total, correct)
    }
}

class QuizViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return QuizViewModel(repository) as T
    }
}
