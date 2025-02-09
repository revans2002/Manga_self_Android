package com.bluebirdcorp.managashelfrev.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluebirdcorp.managashelfrev.model.Manga
import com.bluebirdcorp.managashelfrev.repository.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MangaViewModel(private val repository: MangaRepository) : ViewModel() {
    private val _items = MutableStateFlow<List<Manga>>(emptyList())
    val items: StateFlow<List<Manga>> = _items

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val _allMangas = MutableStateFlow<List<Manga>>(emptyList())
    val allMangas: StateFlow<List<Manga>> = _allMangas


    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState

    private val pageSize = 10

    init {
        loadInitialData()
    }


    /**
     * Loads the first page from the DB.
     * If the DB does not have any items, fetch from the server to update the DB and then reload.
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                // Attempt to load the first page from the DB
                var response = repository.getMangasFromDb(1, pageSize).first()
                // If no data is found, trigger a server fetch
                if (response.items.isEmpty()) {
                    Log.d("MangaViewModel", "DB empty for page 1. Fetching from server...")
                    repository.getMangas(1, pageSize).collect { /* Updates DB */ }
                    // Re-read the page from the DB after the update
                    response = repository.getMangasFromDb(1, pageSize).first()
                }
                _items.value = response.items
                _currentPage.value = response.currentPage
                _totalPages.value = response.totalPages
                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getLikedManga(): LiveData<List<Manga>> {
        return repository.getLikedManga()
    }

    /**
     * Loads the next page from the DB.
     * If the next page is not yet in the DB (or contains fewer than pageSize items),
     * it will trigger a server fetch to update the DB before reading from it.
     */
    fun loadNextPage() {
        if (_currentPage.value < _totalPages.value) {
            viewModelScope.launch {
                _loadingState.value = LoadingState.Loading
                try {
                    val nextPage = _currentPage.value + 1
                    var response = repository.getMangasFromDb(nextPage, pageSize).first()
                    // If no data for this page, fetch from server to update DB
                    if (response.items.isEmpty()) {
                        Log.d("MangaViewModel", "No data for page $nextPage. Fetching new items from server...")
                        repository.getMangas(nextPage, pageSize).collect { /* Updates DB */ }
                        response = repository.getMangasFromDb(nextPage, pageSize).first()
                    }
                    _items.value = response.items
                    _currentPage.value = response.currentPage
                    _totalPages.value = response.totalPages
                    _loadingState.value = LoadingState.Success
                } catch (e: Exception) {
                    _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Loads the previous page from the DB.
     */
    fun loadPreviousPage() {
        if (_currentPage.value > 1) {
            viewModelScope.launch {
                _loadingState.value = LoadingState.Loading
                try {
                    val previousPage = _currentPage.value - 1
                    val response = repository.getMangasFromDb(previousPage, pageSize).first()
                    _items.value = response.items
                    _currentPage.value = response.currentPage
                    _totalPages.value = response.totalPages
                    _loadingState.value = LoadingState.Success
                } catch (e: Exception) {
                    _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Update a single Manga item in the DB.
     */
    fun updateManga(manga: Manga) {
        viewModelScope.launch {
            repository.updateManga(manga)
        }
    }

    fun loadAllMangas() {
        viewModelScope.launch {
            repository.getAllMangasWithoutPaging().collect { mangas ->
                _allMangas.value = mangas
            }
        }
    }

    fun sortByScoreAscending() {
        viewModelScope.launch {
            allMangas.value?.let { mangaList ->
                val sortedList = mangaList.sortedBy { it.score }
                _allMangas.emit(sortedList)
            }
        }
    }

    fun sortByScoreDescending() {
        viewModelScope.launch {
            allMangas.value?.let { mangaList ->
                val sortedList = mangaList.sortedByDescending { it.score }
                _allMangas.emit(sortedList)
            }
        }
    }

    fun sortByPopularityAscending() {
        viewModelScope.launch {
            allMangas.value?.let { mangaList ->
                val sortedList = mangaList.sortedBy { it.popularity }
                _allMangas.emit(sortedList)
            }
        }
    }

    fun sortByPopularityDescending() {
        viewModelScope.launch {
            allMangas.value?.let { mangaList ->
                val sortedList = mangaList.sortedByDescending { it.popularity }
                _allMangas.emit(sortedList)
            }
        }
    }

}
