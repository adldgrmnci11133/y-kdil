package com.yokdil.vocab.ui.progress

import androidx.lifecycle.*
import com.yokdil.vocab.data.repository.WordRepository

class ProgressViewModel(private val repository: WordRepository) : ViewModel() {
    val learnedCount: LiveData<Int> = repository.learnedWordCount
    val totalCount: LiveData<Int> = repository.totalWordCount
    val totalCorrect: LiveData<Int> = repository.totalCorrect
    val totalWrong: LiveData<Int> = repository.totalWrong
}

class ProgressViewModelFactory(private val repository: WordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProgressViewModel(repository) as T
    }
}
