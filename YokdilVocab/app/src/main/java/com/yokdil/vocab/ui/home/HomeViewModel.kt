package com.yokdil.vocab.ui.home

import androidx.lifecycle.*
import com.yokdil.vocab.data.repository.WordRepository

class HomeViewModel(repository: WordRepository) : ViewModel() {
    val learnedCount: LiveData<Int> = repository.learnedWordCount
    val totalCount: LiveData<Int> = repository.totalWordCount
}

class HomeViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repository) as T
    }
}
