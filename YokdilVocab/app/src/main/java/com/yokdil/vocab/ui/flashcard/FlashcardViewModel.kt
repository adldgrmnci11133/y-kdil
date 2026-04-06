package com.yokdil.vocab.ui.flashcard

import androidx.lifecycle.*
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.data.repository.WordRepository
import kotlinx.coroutines.launch

class FlashcardViewModel(private val repository: WordRepository) : ViewModel() {

    private val _words = MutableLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _isFlipped = MutableLiveData(false)
    val isFlipped: LiveData<Boolean> = _isFlipped

    val currentWord: LiveData<Word?> = MediatorLiveData<Word?>().apply {
        fun update() {
            val list = _words.value ?: return
            val idx = _currentIndex.value ?: 0
            value = if (list.isNotEmpty() && idx < list.size) list[idx] else null
        }
        addSource(_words) { update() }
        addSource(_currentIndex) { update() }
    }

    val progress: LiveData<Pair<Int, Int>> = MediatorLiveData<Pair<Int, Int>>().apply {
        fun update() {
            val total = _words.value?.size ?: 0
            val current = (_currentIndex.value ?: 0) + 1
            value = Pair(current.coerceAtMost(total), total)
        }
        addSource(_words) { update() }
        addSource(_currentIndex) { update() }
    }

    fun loadWords(count: Int = 20) {
        viewModelScope.launch {
            val list = repository.getWordsForFlashcard(count)
            _words.value = list
            _currentIndex.value = 0
            _isFlipped.value = false
        }
    }

    fun flip() {
        _isFlipped.value = !(_isFlipped.value ?: false)
    }

    fun markKnown() {
        val word = currentWord.value ?: return
        viewModelScope.launch {
            repository.markLearned(word.id, true)
            repository.recordAnswer(word.id, true)
            moveNext()
        }
    }

    fun markUnknown() {
        val word = currentWord.value ?: return
        viewModelScope.launch {
            repository.recordAnswer(word.id, false)
            moveNext()
        }
    }

    fun toggleFavorite() {
        val word = currentWord.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(word.id, !word.isFavorite)
            // Reload to reflect updated state
            val updatedList = _words.value?.toMutableList() ?: return@launch
            val idx = _currentIndex.value ?: 0
            val updatedWord = repository.getWordById(word.id)
            updatedWord?.let { updatedList[idx] = it }
            _words.value = updatedList
        }
    }

    private fun moveNext() {
        val list = _words.value ?: return
        val idx = _currentIndex.value ?: 0
        if (idx + 1 < list.size) {
            _currentIndex.value = idx + 1
            _isFlipped.value = false
        } else {
            _currentIndex.value = list.size // signals completion
        }
    }

    val isFinished: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        fun update() {
            val total = _words.value?.size ?: 0
            val idx = _currentIndex.value ?: 0
            value = total > 0 && idx >= total
        }
        addSource(_words) { update() }
        addSource(_currentIndex) { update() }
    }
}

class FlashcardViewModelFactory(private val repository: WordRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FlashcardViewModel(repository) as T
    }
}
