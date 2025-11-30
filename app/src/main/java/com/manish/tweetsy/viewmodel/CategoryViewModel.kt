package com.manish.tweetsy.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manish.tweetsy.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: TweetRepository
) : ViewModel() {

    val categories: StateFlow<List<String>?>
        get() = repository.categories

    init {
        viewModelScope.launch {
            repository.getCategories()

        }
    }



}