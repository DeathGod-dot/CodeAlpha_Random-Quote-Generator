package com.example.yumelines.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumelines.data.Quote
import com.example.yumelines.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuoteUiState(
    val quote: Quote? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class QuoteViewModel : ViewModel() {
    private val repository = QuoteRepository()
    
    private val _uiState = MutableStateFlow(QuoteUiState(isLoading = true))
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()
    
    init {
        fetchRandomQuote()
    }
    
    fun fetchRandomQuote() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            repository.getRandomQuote()
                .onSuccess { quote ->
                    _uiState.value = _uiState.value.copy(
                        quote = quote,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }
}
