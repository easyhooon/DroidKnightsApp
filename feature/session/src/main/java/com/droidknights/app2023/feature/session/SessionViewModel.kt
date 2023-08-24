package com.droidknights.app2023.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidknights.app2023.core.domain.usecase.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val getSessionsUseCase: GetSessionsUseCase,
) : ViewModel() {

    private val errorStateChannel = Channel<SessionUiState.Error>()
    val errorStateFlow get() = errorStateChannel.receiveAsFlow()

    val uiState: StateFlow<SessionUiState> = flow { emit(getSessionsUseCase().toPersistentList()) }
        .map(SessionUiState::Sessions)
        .catch { throwable ->
            errorStateChannel.send(SessionUiState.Error(throwable))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionUiState.Loading
        )
}
