package com.yokdil.vocab.ui.detail

import androidx.lifecycle.*
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.data.repository.WordRepository
import kotlinx.coroutines.launch

class WordDetailViewModel(private val repository: WordRepository) : ViewModel() {

    private val _word = MutableLiveData<Word?>()
    val word: LiveData<Word?> = _word

    fun loadWord(id: Int) {
        viewModelScope.launch {
            _word.value = repository.getWordById(id)
        }
    }

    fun toggleFavorite(word: Word) {
        viewModelScope.launch {
            repository.toggleFavorite(word.id, !word.isFavorite)
            _word.value = repository.getWordById(word.id)
        }
    }
}

class WordDetailViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WordDetailViewModel(repository) as T
    }
}
