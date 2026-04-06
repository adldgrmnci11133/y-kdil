package com.yokdil.vocab.ui.favorites

import androidx.lifecycle.*
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.data.repository.WordRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: WordRepository) : ViewModel() {

    private val _words = MediatorLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words

    private var allFavorites: List<Word> = emptyList()
    private var isShowingAll = true
    private var searchQuery = ""

    init {
        _words.addSource(repository.favoriteWords) { list ->
            allFavorites = list
            applyFilter()
        }
    }

    fun search(query: String) {
        searchQuery = query
        applyFilter()
    }

    fun showAll() {
        isShowingAll = true
        applyFilter()
    }

    fun showUnlearned() {
        isShowingAll = false
        applyFilter()
    }

    private fun applyFilter() {
        var filtered = allFavorites
        if (!isShowingAll) filtered = filtered.filter { !it.isLearned }
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.word.contains(searchQuery, true) || it.turkishMeaning.contains(searchQuery, true)
            }
        }
        _words.value = filtered
    }

    fun toggleFavorite(word: Word) {
        viewModelScope.launch {
            repository.toggleFavorite(word.id, !word.isFavorite)
        }
    }
}

class FavoritesViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FavoritesViewModel(repository) as T
    }
}
