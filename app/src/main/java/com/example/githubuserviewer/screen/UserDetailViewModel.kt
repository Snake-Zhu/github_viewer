package com.example.githubuserviewer.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserDetailViewModel: ViewModel() {
    private val _state = MutableStateFlow(
        UserDetailState(
            UserDetail("", "", "", 0, 0),
            emptyList()
        )
    )
    val state = _state.asStateFlow()
    private val useCase = UserDetailUseCase()

    private fun currentState() = _state.value
    private fun updateState(newState: () -> UserDetailState) {
        _state.value = newState()
    }

    fun loadUserDetail(url: String) {
        viewModelScope.launch(Dispatchers.Default) {
            updateState {
                val userDetail = useCase.getUserDetail(url)
                val current = currentState()
                UserDetailState(userDetail, current.repos)
            }
        }
    }

    fun loadUserRepos(url: String) {
        viewModelScope.launch(Dispatchers.Default) {
            updateState {
                val repos = useCase.getUserRepos(url)
                val userOwnRepos = repos.filter { !it.fork }
                val current = currentState()
                UserDetailState(current.userDetail, userOwnRepos)
            }
        }
    }
}