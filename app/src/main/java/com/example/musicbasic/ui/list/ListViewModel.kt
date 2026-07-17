package com.example.musicbasic.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicbasic.model.Music
import com.example.musicbasic.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ListViewModel
    @Inject
    constructor(
        private val musicRepository: MusicRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ListState())
        val uiState: StateFlow<ListState> = _uiState.asStateFlow()

        private val _effect = MutableSharedFlow<ListEffect>()
        val effect: SharedFlow<ListEffect> = _effect.asSharedFlow()

        init {
            loadMusics()
        }

        private fun loadMusics() {
            _uiState.update {
                it.copy(
                    musicList = musicRepository.getMusics().sortLikedFirst(),
                )
            }
        }

        fun onEvent(event: ListEvent) {
            when (event) {
                is ListEvent.DownloadMusic -> {
                    emitSuccess()
                }

                is ListEvent.ToggleLikeMusic -> {
                    toggleLike(id = event.id)
                }

                is ListEvent.ShareMusic -> {
                    emitSuccess()
                }
            }
        }

        private fun toggleLike(id: Int) {
            _uiState.update { currentState ->
                val musicList =
                    currentState.musicList.map { music ->
                        if (music.id == id) {
                            music.copy(isLike = !music.isLike)
                        } else {
                            music
                        }
                    }

                currentState.copy(
                    musicList = musicList.sortLikedFirst(),
                )
            }
        }

        private fun List<Music>.sortLikedFirst(): List<Music> =
            sortedByDescending { it.isLike }

        private fun emitSuccess() {
            viewModelScope.launch {
                _effect.emit(ListEffect.Success)
            }
        }
    }
